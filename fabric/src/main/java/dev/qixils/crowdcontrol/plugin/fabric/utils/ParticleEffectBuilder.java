package dev.qixils.crowdcontrol.plugin.fabric.utils;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;

/**
 * Builds a particle effect.
 *
 * @param <T> The type of particle effect.
 */
@Data
@Accessors(fluent = true, chain = true)
public class ParticleEffectBuilder<T extends ParticleOptions> {
	private final T particleOptions;
	private ServerLevel level;
	private Vector3d position;
	private Vector3f distance = new Vector3f(0, 0, 0);
	private float maxSpeed = .1f;
	private int count = 1;

	@Contract("_ -> this")
	public ParticleEffectBuilder<T> position(Vector3d position) {
		this.position.x = position.x;
		this.position.y = position.y;
		this.position.z = position.z;
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
		level.sendParticles(particleOptions, position.x, position.y, position.z, count, distance.x(), distance.y(), distance.z(), maxSpeed);
	}

	/**
	 * Sends the particle effect to the specified player.
	 *
	 * @param player the player to send the particle effect to
	 * @param force  whether to force the particle effect to render
	 *               (i.e. ignore long distances and the "Minimal" particle effect setting)
	 */
	public void sendTo(ServerPlayer player, boolean force) {
		if (level == null)
			throw new IllegalStateException("No level set");
		if (position == null)
			throw new IllegalStateException("No position set");
		level.sendParticles(player, particleOptions, force, position.x, position.y, position.z, count, distance.x(), distance.y(), distance.z(), maxSpeed);
	}
}
