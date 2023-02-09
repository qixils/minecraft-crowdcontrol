package dev.qixils.crowdcontrol.plugin.sponge7.mc;

import dev.qixils.crowdcontrol.common.mc.CCEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;

public class SpongeEntity implements CCEntity {

	private final Entity entity;

	public SpongeEntity(Entity entity) {
		this.entity = entity;
	}

	@NotNull
	public Entity entity() {
		return entity;
	}

	@Override
	public void kill() {
		entity.remove();
	}
}
