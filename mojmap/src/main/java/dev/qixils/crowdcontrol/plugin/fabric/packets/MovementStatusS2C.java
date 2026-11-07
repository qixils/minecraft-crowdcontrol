package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.packets.MovementStatusPacketS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class MovementStatusS2C extends MovementStatusPacketS2C {
	public static final ResourceLocation PACKET_ID = new ResourceLocation(METADATA.channel());
	public MovementStatusS2C(FriendlyByteBuf buf) { super(buf); }
	public MovementStatusS2C(MovementStatusType type, MovementStatusValue value) { super(type, value); }
}
