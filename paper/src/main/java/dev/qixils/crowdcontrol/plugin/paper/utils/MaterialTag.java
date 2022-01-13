package dev.qixils.crowdcontrol.plugin.paper.utils;

import dev.qixils.crowdcontrol.common.util.KeyedTag;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Locale;

public class MaterialTag extends MappedKeyedTag<Material> {
	public MaterialTag(KeyedTag tag) {
		super(tag, key -> Material.getMaterial(key.value().toUpperCase(Locale.ROOT)));
	}

	public boolean contains(Block block) {
		return contains(block.getType());
	}

	public boolean contains(Location location) {
		return contains(location.getBlock());
	}
}
