package dev.qixils.crowdcontrol.plugin.utils;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class MaterialTag {
	@Getter
	private final EnumSet<Material> materials;

	public MaterialTag() {
		materials = EnumSet.noneOf(Material.class);
	}

	public MaterialTag(Material first, Material... others) {
		materials = EnumSet.of(first, others);
	}

	public MaterialTag(Collection<Material> materials) {
		this.materials = EnumSet.copyOf(materials);
	}

	public boolean matches(Material material) {
		return materials.contains(material);
	}

	public boolean matches(Block block) {
		return matches(block.getType());
	}

	public boolean matches(Location location) {
		return matches(location.getBlock());
	}

	public MaterialTag and(Material first, Material... others) {
		EnumSet<Material> newTag = materials.clone();
		newTag.addAll(EnumSet.of(first, others));
		return new MaterialTag(newTag);
	}

	public MaterialTag and(Collection<Material> other) {
		Set<Material> newTag = materials.clone();
		newTag.addAll(other);
		return new MaterialTag(newTag);
	}

	public MaterialTag and(MaterialTag other) {
		return and(other.materials);
	}
}
