package dev.qixils.crowdcontrol.plugin.fabric.utils;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * Builds a particle effect.
 *
 * @param <T> The type of particle effect.
 */
@Data
@Accessors(fluent = true, chain = true)
public class ParticleEffectBuilder<T extends ParticleEffect> {
	private final @NotNull T particleOptions;
	private @Nullable ServerWorld level = null;
	private @Nullable Vector3d position = null;
	private @NotNull Vector3f distance = new Vector3f(0, 0, 0);
	private float maxSpeed = .1f;
	private int count = 1;

	@Contract("_ -> this")
	public ParticleEffectBuilder<T> position(Vector3d position) {
		this.position = new Vector3d(position.x, position.y, position.z);
		return this;
	}

	@Contract("_ -> this")
	public ParticleEffectBuilder<T> distance(Vector3f distance) {
		this.distance.set(distance.x(), distance.y(), distance.z());
		return this;
	}

	@Contract("_, _, _ -> this")
	public ParticleEffectBuilder<T> distance(float x, float y, float z) {
		this.distance.set(x, y, z);
		return this;
	}

	@Contract("_ -> this")
	public ParticleEffectBuilder<T> xDistance(float x) {
		this.distance.set(x, distance.y(), distance.z());
		return this;
	}

	@Contract("_ -> this")
	public ParticleEffectBuilder<T> yDistance(float y) {
		this.distance.set(distance.x(), y, distance.z());
		return this;
	}

	@Contract("_ -> this")
	public ParticleEffectBuilder<T> zDistance(float z) {
		this.distance.set(distance.x(), distance.y(), z);
		return this;
	}

	@Contract("_ -> this")
	public ParticleEffectBuilder<T> horizDistance(float distance) {
		return xDistance(distance).zDistance(distance);
	}

	@Contract("_ -> this")
	public ParticleEffectBuilder<T> distance(float distance) {
		this.distance.set(distance, distance, distance);
		return this;
	}

	/**
	 * Sends the particle effect to all players in the level.
	 */
	public void send() {
		if (level == null)
			throw new IllegalStateException("No level set");
		if (position == null)
			throw new IllegalStateException("No position set");
		level.spawnParticles(particleOptions, position.x, position.y, position.z, count, distance.x(), distance.y(), distance.z(), maxSpeed);
	}

	/**
	 * Sends the particle effect to the specified player.
	 *
	 * @param player the player to send the particle effect to
	 * @param force  whether to force the particle effect to render
	 *               (i.e. ignore long distances and the "Minimal" particle effect setting)
	 */
	public void sendTo(ServerPlayerEntity player, boolean force) {
		if (level == null)
			throw new IllegalStateException("No level set");
		if (position == null)
			throw new IllegalStateException("No position set");
		level.spawnParticles(player, particleOptions, force, position.x, position.y, position.z, count, distance.x(), distance.y(), distance.z(), maxSpeed);
	}
}
