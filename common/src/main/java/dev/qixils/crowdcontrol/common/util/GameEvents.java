package dev.qixils.crowdcontrol.common.util;

import live.crowdcontrol.cc4j.websocket.data.CCGameEvent;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.csIdOf;

public class GameEvents {
	private GameEvents() {
	}

	@NotNull
	public static CCGameEvent death() {
		return new CCGameEvent("death", null);
	}

	@NotNull
	public static CCGameEvent kill(Key mob) {
		return new CCGameEvent("kill", Map.of("mob", csIdOf(mob)));
	}

	@NotNull
	public static CCGameEvent advancement(Key achievement) {
		return new CCGameEvent("advancement", Map.of("advancement", achievement.asMinimalString()));
	}
}
