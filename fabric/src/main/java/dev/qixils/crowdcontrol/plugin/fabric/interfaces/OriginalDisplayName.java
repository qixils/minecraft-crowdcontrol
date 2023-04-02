package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface OriginalDisplayName extends dev.onyxstudios.cca.api.v3.component.Component {

	@Nullable Text getValue();

	void setValue(@Nullable Text value);
}
