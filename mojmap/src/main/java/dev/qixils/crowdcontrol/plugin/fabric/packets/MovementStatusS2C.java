package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.packets.MovementStatusPacketS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.resources.ResourceLocation.parse;

public class MovementStatusS2C extends MovementStatusPacketS2C implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, MovementStatusS2C> PACKET_CODEC = CustomPacketPayload.codec(MovementStatusS2C::write, MovementStatusS2C::new);
	public static final Type<MovementStatusS2C> PACKET_ID = new Type<>(parse(METADATA.channel()));
	public @Override @NotNull Type<MovementStatusS2C> type() { return PACKET_ID; }
	public MovementStatusS2C(FriendlyByteBuf buf) { super(buf); }
	public MovementStatusS2C(MovementStatusType type, MovementStatusValue value) { super(type, value); }
}
