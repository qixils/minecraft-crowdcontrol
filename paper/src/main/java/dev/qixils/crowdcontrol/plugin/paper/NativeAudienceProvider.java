package dev.qixils.crowdcontrol.plugin.paper;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

final class NativeAudienceProvider implements AudienceProvider {
	@Override
	public @NotNull Audience all() {
		return (ForwardingAudience.Single) Bukkit::getServer;
	}

	@Override
	public @NotNull Audience console() {
		return (ForwardingAudience.Single) Bukkit::getConsoleSender;
	}

	@Override
	public @NotNull Audience players() {
		return (ForwardingAudience) Bukkit::getOnlinePlayers;
	}

	@Override
	public @NotNull Audience player(@NotNull UUID playerId) {
		return Optional.<Audience>ofNullable(Bukkit.getPlayer(playerId)).orElse(Audience.empty());
	}

	@Override
	public @NotNull Audience permission(@NotNull String permission) {
		return (ForwardingAudience) () -> Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission(permission)).toList();
	}

	@Override
	public @NotNull Audience world(@NotNull Key world) {
		NamespacedKey key = new NamespacedKey(world.namespace(), world.value());
		return (ForwardingAudience.Single) () -> Optional.<Audience>ofNullable(Bukkit.getWorld(key)).orElse(Audience.empty());
	}

	@Override
	public @NotNull Audience server(@NotNull String serverName) {
		return all();
	}

	@Override
	public @NotNull ComponentFlattener flattener() {
		return ComponentFlattener.basic(); // TODO: setup paperweight userdev (not that it matters much)
	}

	@Override
	public void close() {

	}
}
