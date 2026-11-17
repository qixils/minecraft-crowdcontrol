package dev.qixils.crowdcontrol.plugin.fabric.client.lidar;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Draws the LiDAR point cloud over a blacked-out world.
 * <p>
 * Dots on terrain never move, so they are uploaded once into a ring buffer and simply redrawn every
 * frame, with the oldest being overwritten once it fills.
 * Dots on entities fade out shortly after landing, since whatever they hit has probably moved on,
 * so those are rebuilt every frame instead.
 * <p>
 * Everything here must happen on the render thread.
 */
final class LidarRenderer {
	/** The most dots on terrain that are kept at once; about 80 seconds of rays that all hit something. */
	private static final int CAPACITY = 96 * 1024;
	/** The most dots on entities that are kept at once. */
	private static final int ECHO_CAPACITY = 1 << 12;
	/** How long a dot on an entity takes to fade away. */
	private static final long ECHO_LIFETIME_MS = 1500;

	private static final VertexFormat FORMAT = DefaultVertexFormat.POSITION_COLOR;
	private static final int DOT_BYTES = FORMAT.getVertexSize() * 4;
	private static final Vector4fc BLACK = new Vector4f(0, 0, 0, 1);

	private final ArrayDeque<LidarDot> pending = new ArrayDeque<>();
	private final ArrayDeque<Echo> echoes = new ArrayDeque<>();

	private @Nullable GpuBuffer dotBuffer;
	private int nextSlot;
	private int dotCount;

	private @Nullable GpuBuffer echoBuffer;

	/**
	 * Queues a permanent dot to be uploaded on the next frame.
	 */
	void addDot(@NotNull LidarDot dot) {
		// anything beyond capacity would only be overwritten straight away
		if (pending.size() >= CAPACITY) pending.removeFirst();
		pending.addLast(dot);
	}

	/**
	 * Adds a dot that fades out over the next moment.
	 */
	void addEcho(@NotNull LidarDot dot, long now) {
		if (echoes.size() >= ECHO_CAPACITY) echoes.removeFirst();
		echoes.addLast(new Echo(dot, now));
	}

	/**
	 * Forgets every dot and frees the GPU memory holding them.
	 */
	void clear() {
		pending.clear();
		echoes.clear();
		nextSlot = 0;
		dotCount = 0;
		if (dotBuffer != null) {
			dotBuffer.close();
			dotBuffer = null;
		}
		if (echoBuffer != null) {
			echoBuffer.close();
			echoBuffer = null;
		}
	}

	/**
	 * Replaces the level rendered so far with black and the point cloud.
	 *
	 * @param gameRenderer renderer whose level pass just finished
	 * @param anchor       world position that dot coordinates are relative to
	 * @param now          current time, for fading entity dots
	 */
	void render(@NotNull GameRenderer gameRenderer, @NotNull Vec3 anchor, long now) {
		RenderTarget target = gameRenderer.mainRenderTarget();
		GpuTexture colorTexture = target.getColorTexture();
		GpuTextureView colorTextureView = target.getColorTextureView();
		// the dots are depth tested against the world, so drawing without it would show them through walls
		GpuTextureView depthTextureView = target.getDepthTextureView();
		// a target that has been torn down has nothing to draw over in the first place
		if (colorTexture == null || colorTextureView == null || depthTextureView == null) return;

		GpuDevice device = RenderSystem.getDevice();
		CommandEncoder encoder = device.createCommandEncoder();

		// the depth buffer is left alone so that dots stay hidden behind geometry that hasn't been scanned
		encoder.clearColorTexture(colorTexture, BLACK);

		uploadPending(device, encoder);
		int echoCount = uploadEchoes(device, encoder, now);
		int quadCount = Math.max(dotCount, echoCount);
		if (quadCount == 0) return;

		RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
		GpuBuffer indexBuffer = quadIndices.getBuffer(quadCount * 6);

		CameraRenderState camera = gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
		Matrix4f modelView = RenderSystem.getModelViewMatrixCopy()
			.mul(camera.viewRotationMatrix)
			.translate(
				(float) (anchor.x - camera.pos.x),
				(float) (anchor.y - camera.pos.y),
				(float) (anchor.z - camera.pos.z)
			);
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelView);

		try (RenderPass pass = encoder.createRenderPass(
			() -> "Crowd Control LiDAR",
			colorTextureView,
			Optional.empty(),
			depthTextureView,
			OptionalDouble.empty()
		)) {
			// depth-tested, unculled, untextured translucent quads; exactly what's needed here
			pass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.DEBUG_QUADS));
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", transforms);
			pass.setIndexBuffer(indexBuffer, quadIndices.type());
			if (dotCount > 0 && dotBuffer != null) {
				pass.setVertexBuffer(0, dotBuffer.slice());
				pass.drawIndexed(dotCount * 6, 1, 0, 0, 0);
			}
			if (echoCount > 0 && echoBuffer != null) {
				pass.setVertexBuffer(0, echoBuffer.slice());
				pass.drawIndexed(echoCount * 6, 1, 0, 0, 0);
			}
		}
	}

	private void uploadPending(GpuDevice device, CommandEncoder encoder) {
		if (pending.isEmpty()) return;
		if (dotBuffer == null)
			dotBuffer = device.createBuffer(() -> "Crowd Control LiDAR dots", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, (long) CAPACITY * DOT_BYTES);

		// write in runs that stop at the end of the ring
		while (!pending.isEmpty()) {
			int run = Math.min(pending.size(), CAPACITY - nextSlot);
			try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(run * DOT_BYTES)) {
				BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, FORMAT);
				for (int i = 0; i < run; i++)
					pending.removeFirst().emit(builder, 1);
				write(encoder, dotBuffer, (long) nextSlot * DOT_BYTES, builder);
			}
			nextSlot = (nextSlot + run) % CAPACITY;
			dotCount = Math.min(dotCount + run, CAPACITY);
		}
	}

	private int uploadEchoes(GpuDevice device, CommandEncoder encoder, long now) {
		while (!echoes.isEmpty() && now - echoes.peekFirst().createdAt() >= ECHO_LIFETIME_MS)
			echoes.removeFirst();
		if (echoes.isEmpty()) return 0;
		if (echoBuffer == null)
			echoBuffer = device.createBuffer(() -> "Crowd Control LiDAR echoes", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, (long) ECHO_CAPACITY * DOT_BYTES);

		int count = echoes.size();
		try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(count * DOT_BYTES)) {
			BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, FORMAT);
			for (Echo echo : echoes)
				echo.dot().emit(builder, 1 - (float) (now - echo.createdAt()) / ECHO_LIFETIME_MS);
			write(encoder, echoBuffer, 0, builder);
		}
		return count;
	}

	private static void write(CommandEncoder encoder, GpuBuffer buffer, long offset, BufferBuilder builder) {
		try (MeshData mesh = builder.buildOrThrow()) {
			encoder.writeToBuffer(buffer.slice(offset, mesh.vertexBuffer().remaining()), mesh.vertexBuffer());
		}
	}

	private record Echo(LidarDot dot, long createdAt) {
	}
}
