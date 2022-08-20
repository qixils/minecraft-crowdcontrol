package dev.qixils.crowdcontrol.plugin.fabric.mc;

import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class FabricPlayer extends FabricLivingEntity implements CCPlayer {

	public FabricPlayer(ServerPlayer entity) {
		super(entity);
	}

	@Override
	public @NotNull ServerPlayer entity() {
		return (ServerPlayer) super.entity();
	}
}
