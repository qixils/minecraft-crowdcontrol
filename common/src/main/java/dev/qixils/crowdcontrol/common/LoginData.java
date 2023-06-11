package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.common.util.UUIDUtil;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Data
public final class LoginData {
	private final @Nullable UUID id;
	private final @Nullable String name;

	public LoginData(String login) {
		String[] split = login.split(":");
		if (split.length == 1) {
			this.id = UUIDUtil.parseUUID(split[0]);
			this.name = this.id == null ? split[0] : null;
		} else {
			this.id = UUIDUtil.parseUUID(split[0]);
			this.name = split[1];
		}
	}
}
