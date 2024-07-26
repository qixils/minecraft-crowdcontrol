package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.util.SemVer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static dev.qixils.crowdcontrol.common.Plugin.VERSION_RESPONSE_SIZE;
import static dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin.VERSION_RESPONSE_ID;

public record ResponseVersionC2S(@NotNull SemVer version) implements CustomPacketPayload {
	// boilerplate
	public static final StreamCodec<RegistryFriendlyByteBuf, ResponseVersionC2S> PACKET_CODEC = ByteBufCodecs.stringUtf8(VERSION_RESPONSE_SIZE).map(ResponseVersionC2S::new, ResponseVersionC2S::versionString).cast();
	public static final Type<ResponseVersionC2S> PACKET_ID = new Type<>(VERSION_RESPONSE_ID);
	public @Override @NotNull Type<ResponseVersionC2S> type() { return PACKET_ID; }

	// util
	public ResponseVersionC2S(@NotNull String version) {
		this(new SemVer(version));
	}

	public @NotNull String versionString() {
		return version.toString();
	}
}
