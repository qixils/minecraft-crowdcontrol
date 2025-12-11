package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.SetLanguagePacketS2C;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static net.minecraft.resources.Identifier.parse;

public class SetLanguageS2C extends SetLanguagePacketS2C implements CustomPacketPayload {
	// boilerplate
	public static final StreamCodec<RegistryFriendlyByteBuf, SetLanguageS2C> PACKET_CODEC = CustomPacketPayload.codec(SetLanguageS2C::write, SetLanguageS2C::new);
	public static final Type<SetLanguageS2C> PACKET_ID = new Type<>(parse(METADATA.channel()));
	public @Override @NotNull Type<SetLanguageS2C> type() { return PACKET_ID; }
	public SetLanguageS2C(FriendlyByteBuf buf) { super(buf); }
	public SetLanguageS2C(LanguageState state, Duration duration) { super(state, duration); }
}
