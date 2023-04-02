package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.commands.FreezeCommand;
import dev.qixils.crowdcontrol.plugin.fabric.commands.FreezeCommand.FreezeData;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

import static dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil.keepInventoryRedirect;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	void onTick(CallbackInfo ci) {
		//noinspection ConstantConditions
		ServerPlayerEntity thiss = (ServerPlayerEntity) (Object) this;
		UUID uuid = thiss.getUuid();

		if (!FreezeCommand.DATA.containsKey(uuid))
			return;

		List<FreezeData> data = FreezeCommand.DATA.get(uuid);
		if (data.isEmpty())
			return;

		Location cur = new Location(thiss);
		Location dest = cur;
		for (FreezeData datum : data)
			dest = datum.getDestination(dest);

		if (!cur.level().equals(dest.level()))
			return;

		if (!cur.equals(dest))
			dest.teleportHere(thiss);

		for (FreezeData datum : data)
			datum.previousLocation = dest;
	}

	@Redirect(
			method = "copyFrom",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z")
	)
	private boolean restoreFromRedirectKeepInventory(GameRules gameRules, GameRules.Key<BooleanRule> key) {
		return keepInventoryRedirect((Entity) (Object) this, gameRules, key);
	}

	@Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
	private void callDeathEvent(final DamageSource source, final CallbackInfo ci) {
		EntityUtil.handleDie((LivingEntity) (Object) this, source, ci);
	}

	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	private void callDamageEvent(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
		EntityUtil.handleDamage((Entity) (Object) this, source, amount, cir);
	}
}
