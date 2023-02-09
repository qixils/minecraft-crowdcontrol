package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface OriginalDisplayName extends dev.onyxstudios.cca.api.v3.component.Component {

	@Nullable Component getValue();

	void setValue(@Nullable Component value);
}
