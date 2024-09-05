package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface OriginalDisplayName {

	@Nullable
	default Component cc$getOriginalDisplayName() {
		return null;
	}

	default void cc$setOriginalDisplayName(@Nullable Component value) {

	}
}
