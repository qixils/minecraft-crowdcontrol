package dev.qixils.crowdcontrol;

import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Data;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

@Data
public class TrackedEffect {
	private final @NotNull Audience audience;
	private final @NotNull PublicEffectPayload request;
	private final @NotNull CCPlayer ccPlayer;
}
