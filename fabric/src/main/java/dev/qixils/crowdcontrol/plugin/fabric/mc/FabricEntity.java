package dev.qixils.crowdcontrol.plugin.fabric.mc;

import dev.qixils.crowdcontrol.common.mc.CCEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class FabricEntity implements CCEntity {

	protected static final Logger logger = LoggerFactory.getLogger(FabricEntity.class);
	private final @NotNull Entity entity;

	@Override
	public void kill() {
		entity.kill();
	}
}
