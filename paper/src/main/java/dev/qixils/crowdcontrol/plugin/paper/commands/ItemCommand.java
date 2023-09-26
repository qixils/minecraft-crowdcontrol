package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil.cbClass;

public interface ItemCommand extends FeatureElementCommand {
	@NotNull Material getItem();

	@Override
	default @NotNull Optional<Object> requiredFeatures() {
//		return CraftMagicNumbers.getItem(getItem()).requiredFeatures();
		return ReflectionUtil.getClazz(cbClass("util.CraftMagicNumbers")).flatMap(clazz -> ReflectionUtil.invokeMethod(
				(Object) null,
				clazz,
				"getItem",
				new Class<?>[]{Material.class},
				getItem()
		));
	}
}
