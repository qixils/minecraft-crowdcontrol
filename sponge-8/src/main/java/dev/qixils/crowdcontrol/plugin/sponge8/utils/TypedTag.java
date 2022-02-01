package dev.qixils.crowdcontrol.plugin.sponge8.utils;

import dev.qixils.crowdcontrol.common.util.KeyedTag;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryType;

public class TypedTag<T> extends MappedKeyedTag<T> {
	public TypedTag(KeyedTag tag, SpongeCrowdControlPlugin plugin, RegistryType<T> registryType) {
		this(tag, plugin.getGame(), registryType);
	}

	public TypedTag(KeyedTag tag, Game game, RegistryType<T> registryType) {
		super(tag, key -> game.registry(registryType).findValue(ResourceKey.resolve(key.toString())).orElse(null));
	}
}
