package dev.qixils.crowdcontrol.plugin.fabric.client;

import net.minecraft.class_8367;
import net.minecraft.class_8373;

import java.util.Map;

public record OptionWrapper(class_8373 id, class_8367.class_8368 data) {
	public OptionWrapper(Map.Entry<class_8373, class_8367.class_8368> entry) {
		this(entry.getKey(), entry.getValue());
	}
}
