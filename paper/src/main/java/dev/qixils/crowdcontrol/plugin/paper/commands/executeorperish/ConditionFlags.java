package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@Data
@Builder
public final class ConditionFlags implements Predicate<Player> {
	public static final @NotNull ConditionFlags DEFAULT = ConditionFlags.builder().build();

	@Builder.Default
	private final @Nullable List<World.Environment> allowedDimensions = null;
	@Builder.Default
	private final @Nullable Collection<Material> requiredItems = null;

	@SuppressWarnings("ConstantConditions") // IntelliJ thinks the parameters are non-null for some reason
	@Override
	public boolean test(@NotNull Player player) {
		if (allowedDimensions != null && !allowedDimensions.contains(player.getWorld().getEnvironment()))
			return false;
		if (requiredItems != null) {
			for (Material material : requiredItems) {
				// TODO: does this check armor & offhand (and does it matter if it doesn't?)
				if (!player.getInventory().contains(material))
					return false;
			}
		}
		return true;
	}
}
