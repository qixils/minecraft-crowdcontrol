package dev.qixils.crowdcontrol.plugin.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;

import java.util.List;
import java.util.Optional;

@Getter
public class SoundCommand extends ImmediateCommand {
	private final String effectName = "sfx";
	private final String displayName = "Spooky Sound Effect";
	// lots of the spooky sounds are missing (or, more likely, renamed) from this old version,
	// so I'm using a mapped tag just to ensure sounds exist (so the purchase does not go to waste)
	private final MappedKeyedTag<SoundType> spookySounds;

	public SoundCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
		spookySounds = new MappedKeyedTag<>(
				CommonTags.SPOOKY_SOUNDS,
				key -> plugin.getRegistry().getType(SoundType.class, key.asString()).orElse(null)
		);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		SoundType sound = spookySounds.getRandom();
		for (Player player : players) {
			Vector3d playAt = player.getPosition();
			Optional<Direction> direction = player.get(Keys.DIRECTION);
			if (direction.isPresent()) {
				playAt = playAt.add(direction.get().getOpposite().asBlockOffset().toDouble());
			}
			player.playSound(
					sound,
					SoundCategories.MASTER,
					playAt,
					1.75f,
					1f,
					1f
			);
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
