package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.Sponge7TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;

@Getter
public class GiveItemCommand extends ImmediateCommand {
	private final ItemType item;
	private final String effectName;
	private final String displayName;

	public GiveItemCommand(SpongeCrowdControlPlugin plugin, ItemType item) {
		super(plugin);
		this.item = item;
		this.effectName = "give_" + Sponge7TextUtil.valueOf(item);
		this.displayName = "Give " + item.getTranslation().get();
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		sync(() -> {
			for (Player player : players) {
				Item entity = (Item) player.getLocation().createEntity(EntityTypes.ITEM);
				entity.transform(Keys.REPRESENTED_ITEM, $ -> ItemStack.of(item).createSnapshot());
				// seems like Sponge 7 doesn't support any of the other keys used in the Paper impl
				// so this is a note to add them back in the Sponge 8 impl

				// give entity a cause & spawn it
				try (StackFrame frame = plugin.getGame().getCauseStackManager().pushCauseFrame()) {
					frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
					player.getWorld().spawnEntity(entity);
				}

				// workaround to limit the circulation of end portal frames in the economy
				if (item.equals(ItemTypes.END_PORTAL_FRAME))
					break;
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
