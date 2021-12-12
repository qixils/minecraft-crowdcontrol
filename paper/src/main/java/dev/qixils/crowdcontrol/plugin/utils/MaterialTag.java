package dev.qixils.crowdcontrol.plugin.utils;

import dev.qixils.crowdcontrol.common.util.KeyedTag;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import org.bukkit.Material;

import java.util.Locale;

public class MaterialTag extends MappedKeyedTag<Material> {
	public MaterialTag(KeyedTag tag) {
		super(tag, key -> Material.getMaterial(key.value().toUpperCase(Locale.ROOT)));
	}
}
