package dev.qixils.crowdcontrol.plugin.fabric.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import dev.qixils.crowdcontrol.plugin.fabric.client.fabric.ClientPacketContextImpl;
import dev.qixils.crowdcontrol.plugin.fabric.client.vote.ProposalHandler;
import dev.qixils.crowdcontrol.plugin.fabric.packets.MovementStatusS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.RequestVersionS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetShaderS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.fabric.PacketUtilImpl;
import dev.qixils.crowdcontrol.plugin.fabric.vote.VoteNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class FabricPlatformClient extends ModdedPlatformClient implements ClientModInitializer {

	public static @NotNull FabricPlatformClient get() {
		return (FabricPlatformClient) ModdedPlatformClient.get();
	}

	@Override
	public void onInitializeClient() {
		super.onInitializeClient();

		PacketUtilImpl.registerPackets();

		ClientPlayNetworking.registerGlobalReceiver(RequestVersionS2C.PACKET_ID, (client, handler, buf, responseSender) ->
			client.execute(() -> handleRequestVersion(RequestVersionS2C.INSTANCE, new ClientPacketContextImpl(client, responseSender))));
		ClientPlayNetworking.registerGlobalReceiver(SetShaderS2C.PACKET_ID, (client, handler, buf, responseSender) -> {
			SetShaderS2C payload = new SetShaderS2C(buf);
			client.execute(() -> handleSetShader(payload, new ClientPacketContextImpl(client, responseSender)));
		});
		ClientPlayNetworking.registerGlobalReceiver(MovementStatusS2C.PACKET_ID, (client, handler, buf, responseSender) -> {
			MovementStatusS2C payload = new MovementStatusS2C(buf);
			client.execute(() -> handleMovementStatus(payload, new ClientPacketContextImpl(client, responseSender)));
		});

		// accept default username from the server
		ClientPlayNetworking.registerGlobalReceiver(VoteNetworking.CHANNEL_ID, (client, handler, buf, responseSender) -> {
			String channel = buf.readUtf();
			client.execute(() -> {
				if (channel.isEmpty())
					ProposalHandler.getOptional().ifPresent(ProposalHandler::leaveChannel);
				else
					ProposalHandler.get().joinChannel(channel);
			});
		});
		// lets streamers use the integration on servers without the mod
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("ccvote")
			.then(literal("stop").executes(ctx -> {
				ProposalHandler.getOptional().ifPresent(ProposalHandler::leaveChannel);
				ctx.getSource().sendFeedback(Component.translatableWithFallback("crowdcontrol.proposal.command.stop", "Your Twitch chat will no longer vote on proposals"));
				return 1;
			}))
			.then(argument("channel", StringArgumentType.word()).executes(ctx -> {
				String channel = StringArgumentType.getString(ctx, "channel").toLowerCase(Locale.ROOT);
				ProposalHandler.get().joinChannel(channel);
				ctx.getSource().sendFeedback(Component.translatableWithFallback("crowdcontrol.proposal.command.start", "Twitch chat for %s will now vote on proposals", channel));
				return 1;
			}))));
	}

	public void sendToServer(@NotNull PluginPacket payload) {
		ResourceLocation channel = PacketUtilImpl.channel(payload);
		if (!ClientPlayNetworking.canSend(channel)) return;
		ClientPlayNetworking.send(channel, PacketUtilImpl.encode(payload));
	}
}
