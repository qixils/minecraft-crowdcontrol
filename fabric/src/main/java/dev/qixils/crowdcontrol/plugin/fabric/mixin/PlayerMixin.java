package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import dev.qixils.crowdcontrol.plugin.fabric.packets.MovementStatusS2C;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;

@SuppressWarnings("DataFlowIssue")
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements MovementStatus {

	// dummy constructor
	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	private final @NotNull EnumMap<MovementStatusType, MovementStatusValue> prohibited = new EnumMap<>(MovementStatusType.class);

	@Override
	public @NotNull MovementStatusValue cc$getMovementStatus(@NotNull MovementStatusType type) {
		return prohibited.getOrDefault(type, MovementStatusValue.ALLOWED);
	}

	@Override
	public void cc$setMovementStatus(@NotNull MovementStatusType type, @NotNull MovementStatusValue value) {
		if (value == MovementStatusValue.ALLOWED)
			prohibited.remove(type);
		else
			prohibited.put(type, value);
		if (((Object) this) instanceof ServerPlayer serverPlayer) {
			ServerPlayNetworking.send(serverPlayer, new MovementStatusS2C(type, value));
		}
	}

	@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
	public void jumpFromGround(CallbackInfo ci) {
		Jump event = new Jump((Player) (Object) this, this.level().isClientSide);
		event.fire();
		if (event.cancelled())
			ci.cancel();
	}

	@Unique
	private boolean keepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanValue> key) {
		return EntityUtil.keepInventoryRedirect(this, gameRules, key);
	}

	@Redirect(
			method = "dropEquipment",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
	)
	private boolean equipmentKeepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanValue> key) {
		return keepInventoryRedirect(gameRules, key);
	}

	@Redirect(
			method = "getBaseExperienceReward",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
	)
	private boolean experienceKeepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanValue> key) {
		return keepInventoryRedirect(gameRules, key);
	}
}
