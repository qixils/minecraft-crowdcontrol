package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import dev.qixils.crowdcontrol.plugin.fabric.packets.MovementStatusS2C;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;

@SuppressWarnings("DataFlowIssue")
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin implements MovementStatus {

	// dummy constructor
	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	private final @NotNull EnumMap<MovementStatusType, MovementStatusValue> cc$prohibited = new EnumMap<>(MovementStatusType.class);

	@Override
	public @NotNull MovementStatusValue cc$getMovementStatus(@NotNull MovementStatusType type) {
		return cc$prohibited.getOrDefault(type, MovementStatusValue.ALLOWED);
	}

	@Override
	public void cc$setMovementStatus(@NotNull MovementStatusType type, @NotNull MovementStatusValue value) {
		if (value == MovementStatusValue.ALLOWED)
			cc$prohibited.remove(type);
		else
			cc$prohibited.put(type, value);
		if (((Object) this) instanceof ServerPlayer serverPlayer) {
			ModdedCrowdControlPlugin.sendToPlayerStatic(serverPlayer, new MovementStatusS2C(type, value));
		}
	}

	@Override
	public void jumpFromGround(CallbackInfo ci) {
		Jump event = new Jump((Player) (Object) this, this.level().isClientSide);

		boolean cantJump = cc$getMovementStatus(MovementStatusType.JUMP) == MovementStatusValue.DENIED;
		boolean cantWalk = cc$getMovementStatus(MovementStatusType.WALK) == MovementStatusValue.DENIED;
		if (cantJump || cantWalk) {
			event.cancel();
			if (!event.isClientSide() && ((Object)this) instanceof ServerPlayer sPlayer /* not necessary for clients */ && !cantWalk /* avoids teleporting twice */) {
				sPlayer.connection.teleport(getX(), getY(), getZ(), getYRot(), getXRot());
			}
		}

		event.fire();
		if (event.cancelled())
			ci.cancel();
	}

	@WrapOperation(
			method = "dropEquipment",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
	)
	private boolean equipmentKeepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanValue> key, Operation<Boolean> original) {
		return EntityUtil.keepInventoryRedirect(this, original.call(gameRules, key), key);
	}

	@WrapOperation(
			method = "getBaseExperienceReward",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
	)
	private boolean experienceKeepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanValue> key, Operation<Boolean> original) {
		return EntityUtil.keepInventoryRedirect(this, original.call(gameRules, key), key);
	}
}
