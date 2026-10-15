package dev.qixils.crowdcontrol.common.command;

import live.crowdcontrol.cc4j.websocket.payload.CCEffectDescription;
import live.crowdcontrol.cc4j.websocket.payload.CCUserRecord;
import live.crowdcontrol.cc4j.websocket.payload.ProfileType;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * An effect request made through {@code /crowdcontrol execute}.
 * <p>
 * It is aimed straight at the player who ran the command rather than at whoever is linked to the
 * target account, so that effects can be tested without a linked account or a running session.
 */
@Getter
public class DebugEffectPayload extends PublicEffectPayload {

	/**
	 * The player the effect is aimed at.
	 */
	private final transient @NotNull UUID player;

	/**
	 * Creates a debug request.
	 *
	 * @param player     the player to aim the effect at
	 * @param playerName the name of that player
	 * @param effect     the effect to run
	 */
	public DebugEffectPayload(@NotNull UUID player, @NotNull String playerName, @NotNull CCEffectDescription effect) {
		super(
			UUID.randomUUID(),
			0L,
			effect,
			// never used to find players, since those come from the field above
			new CCUserRecord("ccuid-00000000000000000000000000", playerName, ProfileType.UNKNOWN, "", ""),
			null,
			null,
			false,
			1
		);
		this.player = player;
	}
}
