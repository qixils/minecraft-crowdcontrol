package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.common.util.KeyedTag;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TypedTag<T> extends MappedKeyedTag<T> {
	public TypedTag(KeyedTag tag, Registry<T> registry) {
		super(tag, key -> registry.get(new Identifier(key.namespace(), key.value())));
	}
}
