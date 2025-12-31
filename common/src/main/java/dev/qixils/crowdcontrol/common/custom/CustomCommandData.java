package dev.qixils.crowdcontrol.common.custom;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public record CustomCommandData(
	Component name,
	int price,
	@Nullable String description,
	@Nullable String image,
	@Nullable List<@NotNull String> category,
	List<CustomCommandAction> actions
) {}
