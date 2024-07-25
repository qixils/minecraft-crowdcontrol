package dev.qixils.crowdcontrol.plugin.paper.utils;

import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;

import static dev.qixils.crowdcontrol.common.util.RandomUtil.randomElementFrom;
import static io.papermc.paper.registry.RegistryAccess.registryAccess;

public class RegistryUtil {
	public static <T extends Keyed> Registry<T> get(RegistryKey<T> key) {
		return registryAccess().getRegistry(key);
	}

	public static <T extends Keyed> T random(RegistryKey<T> key) {
		return randomElementFrom(get(key));
	}
}
