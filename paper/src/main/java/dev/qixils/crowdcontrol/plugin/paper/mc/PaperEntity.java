package dev.qixils.crowdcontrol.plugin.paper.mc;

import dev.qixils.crowdcontrol.common.mc.CCEntity;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaperEntity implements CCEntity {

	protected static final Logger logger = LoggerFactory.getLogger("CrowdControl/Entity");

	private final PaperCrowdControlPlugin plugin;
	private final Entity entity;

	public PaperEntity(@NotNull PaperCrowdControlPlugin plugin, @NotNull Entity entity) {
		this.plugin = plugin;
		this.entity = entity;
	}

	@NotNull
	public Entity entity() {
		return entity;
	}

	@Override
	public void execute(@NotNull Runnable runnable, @Nullable Runnable onCancel) {
		entity().getScheduler().run(plugin.getPaperPlugin(), $ -> runnable.run(), onCancel);
	}

	@Override
	public void kill() {
		execute(entity::remove);
	}
}
