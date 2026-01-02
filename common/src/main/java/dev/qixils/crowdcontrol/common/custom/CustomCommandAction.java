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

	@Contract("_, !null -> !null")
	private String _getString(String name, @Nullable String def) {
		if (options == null) return def;

		try {
			String value = options.node(name).getString();
			if (value == null) return def;
			return value;
		} catch (Exception ignored) {
			return def;
		}
	}

	@Contract("_, !null -> !null")
	public String getString(String name, @Nullable String def) {
		String value = _getString(name, def);
		if (value == null) return null;
		return value.trim();
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
