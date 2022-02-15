package dev.qixils.crowdcontrol.common;

import com.google.gson.Gson;
import dev.qixils.crowdcontrol.socket.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class ServerStatus implements JsonObject {
	private static final Gson GSON = new Gson();
	private final boolean GlobalEffects;
	private final @NotNull List<String> RegisteredEffects;

	ServerStatus(boolean globalEffectsEnabled, @NotNull List<String> RegisteredEffects) {
		this.GlobalEffects = globalEffectsEnabled;
		this.RegisteredEffects = RegisteredEffects;
	}

	public boolean globalEffectsEnabled() {
		return GlobalEffects;
	}

	public @NotNull List<String> getRegisteredEffects() {
		return RegisteredEffects;
	}

	@Override
	public @NotNull String toJSON() {
		return GSON.toJson(this);
	}
}
