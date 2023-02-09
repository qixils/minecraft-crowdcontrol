package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl.GameTypeEffectComponentImpl;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl.MovementStatusImpl;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl.OriginalDisplayNameImpl;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl.ViewerMobImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public final class Components implements EntityComponentInitializer {
	// DON'T FORGET THESE NEED TO BE DECLARED IN fabric.mod.json
	public static final @NotNull ComponentKey<ViewerMob> VIEWER_MOB = ComponentRegistry.getOrCreate(new ResourceLocation("crowd-control", "viewer-mob"), ViewerMob.class);
	public static final @NotNull ComponentKey<OriginalDisplayName> ORIGINAL_DISPLAY_NAME = ComponentRegistry.getOrCreate(new ResourceLocation("crowd-control", "original-display-name"), OriginalDisplayName.class);
	public static final @NotNull ComponentKey<GameTypeEffectComponent> GAME_TYPE_EFFECT = ComponentRegistry.getOrCreate(new ResourceLocation("crowd-control", "game-type-effect"), GameTypeEffectComponent.class);
	public static final @NotNull ComponentKey<MovementStatus> MOVEMENT_STATUS = ComponentRegistry.getOrCreate(new ResourceLocation("crowd-control", "movement-status"), MovementStatus.class);

	@Override
	public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
		registry.registerFor(LivingEntity.class, VIEWER_MOB, entity -> new ViewerMobImpl());
		registry.registerFor(LivingEntity.class, ORIGINAL_DISPLAY_NAME, entity -> new OriginalDisplayNameImpl());
		registry.registerForPlayers(GAME_TYPE_EFFECT, player -> new GameTypeEffectComponentImpl());
		registry.registerForPlayers(MOVEMENT_STATUS, MovementStatusImpl::new);
	}
}
