package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
	private final @NotNull Map<Material, Integer> requiredItems = Collections.emptyMap();

	@Override
	public boolean test(@NotNull Player player) {
		if (!allowedDimensions.isEmpty() && !allowedDimensions.contains(player.getWorld().getEnvironment()))
			return false;
		for (Map.Entry<Material, Integer> entry : requiredItems.entrySet()) {
			Material item = entry.getKey();
			int required = entry.getValue();
			int count = 0;
			for (ItemStack stack : player.getInventory().getContents()) {
				if (stack != null && stack.getType() == item) {
					count += stack.getAmount();
					if (count >= required)
						break;
				}
			}
			if (count < required)
				return false;
		}
		return true;
	}
}
