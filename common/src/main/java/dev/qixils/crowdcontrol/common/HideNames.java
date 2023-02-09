package dev.qixils.crowdcontrol.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@AllArgsConstructor
@Getter
public enum HideNames {
	NONE("none", false, false),
	CHAT("chat", true, false),
	ALL("all", true, true);

	private final @NotNull String configCode;
	private final boolean hideChat;
	private final boolean hideOther;

	public static @NotNull HideNames fromConfigCode(@NotNull String configCode) {
		configCode = configCode.toLowerCase(Locale.ENGLISH);
		for (HideNames hideNames : values()) {
			if (hideNames.configCode.equals(configCode)) {
				return hideNames;
			}
		}
		return NONE;
	}
}
