package dev.qixils.crowdcontrol.common.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DynamicForwardingAudience implements ForwardingAudience.Single {
	private final Supplier<Audience> audienceSupplier;

	public DynamicForwardingAudience(Supplier<Audience> audienceSupplier) {
		this.audienceSupplier = audienceSupplier;
	}

	@Override
	public @NotNull Audience audience() {
		try {
			return audienceSupplier.get();
		} catch (Exception e) {
			return Audience.empty();
		}
	}
}
