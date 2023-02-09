package dev.qixils.crowdcontrol.common;

import com.google.gson.Gson;
import dev.qixils.crowdcontrol.socket.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class ServerStatus implements JsonObject {
	private static final Gson GSON = new Gson();
	private final boolean GlobalEffects;
	private final boolean ClientEffects;
	private final @NotNull List<String> RegisteredEffects;

	ServerStatus(boolean globalEffectsEnabled, boolean clientEffectsEnabled, @NotNull List<String> registeredEffects) {
		this.GlobalEffects = globalEffectsEnabled;
		this.ClientEffects = clientEffectsEnabled;
		this.RegisteredEffects = registeredEffects;
	}

	public boolean globalEffectsEnabled() {
		return GlobalEffects;
	}

	public boolean clientEffectsEnabled() {
		return ClientEffects;
	}

	public @NotNull List<String> getRegisteredEffects() {
		return RegisteredEffects;
	}

	@Override
	public @NotNull String toJSON() {
		return GSON.toJson(this);
	}
}
