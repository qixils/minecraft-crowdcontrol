package dev.qixils.crowdcontrol.plugin.mojmap.utils;

import dev.qixils.crowdcontrol.common.util.KeyedTag;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class TypedTag<T> extends MappedKeyedTag<T> {
	public TypedTag(KeyedTag tag, Registry<T> registry) {
		super(tag, key -> registry.get(new ResourceLocation(key.namespace(), key.value())));
	}
}
