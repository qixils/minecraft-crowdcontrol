package dev.qixils.crowdcontrol.common.custom;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record CustomCommandAction(String type, @Nullable ConfigurationNode options) {
	@Contract("_, _, !null -> !null")
	public <T> T getOption(String name, Class<T> clazz, @Nullable T def) {
		if (options == null) return def;

		try {
			T value = options.node(name).get(clazz);
			if (value == null) return def;
			return value;
		} catch (Exception ignored) {
			return def;
		}
	}

	public int getInt(String name, int def) {
		if (options == null) return def;

		try {
			return options.node(name).getInt(def);
		} catch (Exception ignored) {
			return def;
		}
	}
}
