package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface LivingEntityData {

	Optional<Component> originalDisplayName();

	void originalDisplayName(@Nullable Component value);

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	void originalDisplayName(Optional<Component> value);

	boolean viewerSpawned();

	void viewerSpawned(boolean value);
}
