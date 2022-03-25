package dev.qixils.crowdcontrol.plugin.mojmap.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class WrappedAudienceProvider implements AudienceProvider {
	private final AudienceProvider provider;

	protected WrappedAudienceProvider(final AudienceProvider provider) {
		this.provider = provider;
	}

	// wrapped methods

	@Override
	public @NotNull Audience all() {
		return provider.all();
	}

	@Override
	public @NotNull Audience console() {
		return provider.console();
	}

	@Override
	public @NotNull Audience players() {
		return provider.players();
	}

	@Override
	public @NotNull Audience player(@NotNull UUID playerId) {
		return provider.player(playerId);
	}

	@Override
	public @NotNull Audience permission(@NotNull String permission) {
		return provider.permission(permission);
	}

	@Override
	public @NotNull Audience world(@NotNull Key world) {
		return provider.world(world);
	}

	@Override
	public @NotNull Audience server(@NotNull String serverName) {
		return provider.server(serverName);
	}

	@Override
	public @NotNull ComponentFlattener flattener() {
		return provider.flattener();
	}

	@Override
	public void close() {
		provider.close();
	}

	// convenience methods

	public @NotNull AudienceProvider provider() {
		return provider;
	}

	public @NotNull Audience player(@NotNull Player player) {
		return player(player.getUUID());
	}

	public @NotNull Audience world(@NotNull Level world) {
		ResourceLocation key = world.dimension().location();
		return world(Key.key(key.getNamespace(), key.getPath()));
	}

	// kyori <-> native methods

	/**
	 * Get a native {@link net.minecraft.network.chat.Component} from an adventure {@link Component}.
	 *
	 * <p>The specific type of the returned component is undefined. For example, it may be a wrapper object.</p>
	 *
	 * @param adventure adventure input
	 * @return native representation
	 * @since 4.0.0
	 */
	public abstract net.minecraft.network.chat.@NotNull Component toNative(final @NotNull Component adventure);

	/**
	 * Get an adventure {@link Component} from a native {@link net.minecraft.network.chat.Component}.
	 *
	 * @param vanilla the native component
	 * @return adventure component
	 * @since 4.0.0
	 */
	public abstract @NotNull Component toAdventure(final net.minecraft.network.chat.@NotNull Component vanilla);
}
