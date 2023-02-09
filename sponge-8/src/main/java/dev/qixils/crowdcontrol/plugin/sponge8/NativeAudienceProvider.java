package dev.qixils.crowdcontrol.plugin.sponge8;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

final class NativeAudienceProvider implements AudienceProvider {
	@Override
	public @NotNull Audience all() {
		return (ForwardingAudience.Single) () -> Sponge.isServerAvailable() ? Sponge.server() : Sponge.systemSubject();
	}

	@Override
	public @NotNull Audience console() {
		return Sponge.systemSubject();
	}

	@Override
	public @NotNull Audience players() {
		return (ForwardingAudience) () -> Sponge.isServerAvailable() ? ((Iterable<? extends Audience>) Sponge.server().onlinePlayers()) : Collections.emptyList();
	}

	@Override
	public @NotNull Audience player(@NotNull UUID playerId) {
		if (!Sponge.isServerAvailable())
			return Audience.empty();
		Optional<ServerPlayer> player = Sponge.server().player(playerId);
		if (!player.isPresent())
			return Audience.empty();
		return player.get();
	}

	@Override
	public @NotNull Audience permission(@NotNull String permission) {
		return (ForwardingAudience) () -> Sponge.isServerAvailable()
				? (Iterable<? extends Audience>) Sponge.server().onlinePlayers().stream().filter(player -> player.hasPermission(permission)).collect(Collectors.toList())
				: Collections.emptyList();
	}

	@Override
	public @NotNull Audience world(@NotNull Key world) {
		ResourceKey resourceKey = ResourceKey.of(world.namespace(), world.value());
		return (ForwardingAudience.Single) () -> Sponge.isServerAvailable()
				? Sponge.server().worldManager().world(resourceKey).map(serverWorld -> (Audience) serverWorld).orElse(Audience.empty())
				: Audience.empty();
	}

	@Override
	public @NotNull Audience server(@NotNull String serverName) {
		return all();
	}

	@Override
	public @NotNull ComponentFlattener flattener() {
		return SpongeComponents.flattener();
	}

	@Override
	public void close() {
	}
}
