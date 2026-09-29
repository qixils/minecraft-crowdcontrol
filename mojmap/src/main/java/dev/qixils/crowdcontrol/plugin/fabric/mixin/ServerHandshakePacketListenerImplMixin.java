package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.VirtualHost;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplMixin {

	@Shadow @Final private Connection connection;

	@Inject(method = "handleIntention", at = @At("HEAD"))
	private void cc$rememberVirtualHost(ClientIntentionPacket packet, CallbackInfo ci) {
		((VirtualHost) connection).cc$setVirtualHost(cc$cleanHostName(packet.hostName()));
	}

	/**
	 * Proxies such as BungeeCord and older mod loaders tack their own data onto the hostname behind
	 * null bytes, and clients may send a fully qualified name with a trailing dot.
	 */
	@Unique
	private static @Nullable String cc$cleanHostName(@Nullable String hostName) {
		if (hostName == null) return null;
		int end = hostName.indexOf('\0');
		String host = (end == -1 ? hostName : hostName.substring(0, end)).trim();
		while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
		return host.isEmpty() ? null : host;
	}
}
