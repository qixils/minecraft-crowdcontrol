package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.VirtualHost;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Connection.class)
public abstract class ConnectionMixin implements VirtualHost {

	@Unique
	private @Nullable String cc$virtualHost;

	@Override
	public @Nullable String cc$getVirtualHost() {
		return cc$virtualHost;
	}

	@Override
	public void cc$setVirtualHost(@Nullable String host) {
		cc$virtualHost = host;
	}
}
