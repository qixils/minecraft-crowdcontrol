package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.common.util.KeyedTag;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import net.minecraft.core.Registry;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class TypedTag<T> extends MappedKeyedTag<T> {
	public TypedTag(KeyedTag tag, Registry<T> registry) {
		super(tag, key -> registry.get(fromNamespaceAndPath(key.namespace(), key.value())));
	}
}
