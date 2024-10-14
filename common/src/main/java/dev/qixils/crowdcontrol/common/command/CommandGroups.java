package dev.qixils.crowdcontrol.common.command;

import dev.qixils.crowdcontrol.common.command.impl.HealthModifierCommand;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class CommandGroups {
	public static final String[] HEALTH_MODIFIERS = Arrays.stream(HealthModifierCommand.Modifier.values()).map(value -> value.name().toLowerCase(Locale.US)).collect(Collectors.toList()).toArray(new String[]{});
	public static final String[] FREEZE_MODIFIERS = new String[]{
		"camera_lock",
		"camera_lock_to_ground",
		"camera_lock_to_sky",
		"freeze",
		"invert_look",
		"invert_wasd"
	};
}
