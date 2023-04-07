package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
public class CommandListenerMixin {

	// there are technically better ways to do this (it looks like the client does parse an argument array) but this is fine for now
	private static final Pattern LINK_PATTERN = Pattern.compile("account link (\\S+)");
	private static final Pattern UNLINK_PATTERN = Pattern.compile("account unlink (\\S+)");

	@Inject(method = "sendChatCommand", at = @At("HEAD"))
	private void onSendChatCommand(String command, CallbackInfo ci) {
		if (!FabricCrowdControlPlugin.CLIENT_INITIALIZED) return;
		FabricPlatformClient plugin = FabricPlatformClient.get();
		Matcher linkMatcher = LINK_PATTERN.matcher(command);
		if (linkMatcher.matches()) {
			plugin.proposalHandler.joinChannel(linkMatcher.group(1));
			return;
		}
		Matcher unlinkMatcher = UNLINK_PATTERN.matcher(command);
		if (unlinkMatcher.matches()) {
			plugin.proposalHandler.leaveChannel(unlinkMatcher.group(1));
		}
	}
}
