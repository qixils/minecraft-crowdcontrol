package dev.qixils.crowdcontrol.plugin.sponge8.utils;

import dev.qixils.crowdcontrol.common.util.KeyedTag;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;

public class TypedTag<T extends CatalogType> extends MappedKeyedTag<T> {
	public TypedTag(KeyedTag tag, SpongeCrowdControlPlugin plugin, Class<T> typeClass) {
		this(tag, plugin.getGame(), typeClass);
	}

	public TypedTag(KeyedTag tag, Game game, Class<T> typeClass) {
		this(tag, game.getRegistry(), typeClass);
	}

	public TypedTag(KeyedTag tag, GameRegistry registry, Class<T> typeClass) {
		super(tag, key -> registry.getType(typeClass, key.value()).orElse(null));
	}
}
