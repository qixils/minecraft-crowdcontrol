package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.plugin.mojmap.utils.WrappedAudienceProvider;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class FabricWrappedAudienceProvider extends WrappedAudienceProvider {

	public FabricWrappedAudienceProvider(FabricServerAudiences provider) {
		super(provider);
	}

	@Override
	public @NotNull FabricServerAudiences provider() {
		return (FabricServerAudiences) super.provider();
	}

	@Override
	public @NotNull Component toNative(net.kyori.adventure.text.@NotNull Component adventure) {
		return provider().toNative(adventure);
	}

	@Override
	public net.kyori.adventure.text.@NotNull Component toAdventure(@NotNull Component vanilla) {
		return provider().toAdventure(vanilla);
	}

}
