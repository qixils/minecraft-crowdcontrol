package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;

@Data
@Builder
public final class ConditionFlags implements Predicate<Player> {
	public static final @NotNull ConditionFlags DEFAULT = ConditionFlags.builder().build();
	public static final @NotNull ConditionFlags OVERWORLD = ConditionFlags.builder().allowedDimensions(singletonList(World.Environment.NORMAL)).build();
	public static final @NotNull ConditionFlags NETHER = ConditionFlags.builder().allowedDimensions(singletonList(World.Environment.NETHER)).build();
	public static final @NotNull ConditionFlags THE_END = ConditionFlags.builder().allowedDimensions(singletonList(World.Environment.THE_END)).build();

	@Builder.Default
	private final @NotNull List<World.Environment> allowedDimensions = Collections.emptyList();
	@Builder.Default
	private final @NotNull Collection<Material> requiredItems = Collections.emptyList();

	@Override
	public boolean test(@NotNull Player player) {
		if (!allowedDimensions.isEmpty() && !allowedDimensions.contains(player.getWorld().getEnvironment()))
			return false;
		for (Material material : requiredItems) {
			// TODO: does this check armor & offhand (and does it matter if it doesn't?)
			if (!player.getInventory().contains(material))
				return false;
		}
		return true;
	}
}
