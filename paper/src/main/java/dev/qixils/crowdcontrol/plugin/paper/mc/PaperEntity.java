package dev.qixils.crowdcontrol.plugin.paper.mc;

import dev.qixils.crowdcontrol.common.mc.CCEntity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaperEntity implements CCEntity {

	protected static final Logger logger = LoggerFactory.getLogger("CrowdControl/Entity");

	private final Entity entity;

	public PaperEntity(Entity entity) {
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
