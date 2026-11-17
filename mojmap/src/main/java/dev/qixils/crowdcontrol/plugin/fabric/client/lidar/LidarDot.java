package dev.qixils.crowdcontrol.plugin.fabric.client.lidar;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;

/**
 * A single square dot lying flat against whatever surface a ray struck.
 *
 * @param x    center, relative to the effect's anchor
 * @param y    center, relative to the effect's anchor
 * @param z    center, relative to the effect's anchor
 * @param nx   unit normal of the surface
 * @param ny   unit normal of the surface
 * @param nz   unit normal of the surface
 * @param half half the width of the dot
 * @param argb color of the dot
 */
record LidarDot(float x, float y, float z, float nx, float ny, float nz, float half, int argb) {

	/**
	 * Emits this dot as a single quad.
	 *
	 * @param out   consumer to emit vertices to
	 * @param alpha opacity to draw the dot at, from 0 to 1
	 */
	void emit(VertexConsumer out, float alpha) {
		// any two axes perpendicular to the normal will do, since the dot is square
		float ux, uy, uz;
		if (Math.abs(ny) > 0.9F) {
			ux = 1;
			uy = 0;
			uz = 0;
		} else {
			// normal * (0, 1, 0)
			float length = (float) Math.sqrt(nx * nx + nz * nz);
			ux = -nz / length;
			uy = 0;
			uz = nx / length;
		}
		// normal * u
		float vx = ny * uz - nz * uy;
		float vy = nz * ux - nx * uz;
		float vz = nx * uy - ny * ux;

		ux *= half; uy *= half; uz *= half;
		vx *= half; vy *= half; vz *= half;

		int color = ARGB.multiplyAlpha(argb, alpha);
		out.addVertex(x - ux - vx, y - uy - vy, z - uz - vz).setColor(color);
		out.addVertex(x - ux + vx, y - uy + vy, z - uz + vz).setColor(color);
		out.addVertex(x + ux + vx, y + uy + vy, z + uz + vz).setColor(color);
		out.addVertex(x + ux - vx, y + uy - vy, z + uz - vz).setColor(color);
	}
}
