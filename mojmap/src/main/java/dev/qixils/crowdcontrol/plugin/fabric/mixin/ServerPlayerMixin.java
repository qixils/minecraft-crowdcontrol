package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import dev.qixils.crowdcontrol.plugin.fabric.commands.FreezeCommand;
import dev.qixils.crowdcontrol.plugin.fabric.commands.FreezeCommand.FreezeData;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.GameTypeEffectComponent;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

import static dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil.keepInventoryRedirect;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements GameTypeEffectComponent {

	public ServerPlayerMixin(Level level, GameProfile gameProfile) {
		super(level, gameProfile);
	}

	@Unique
	GameType cc$gameTypeEffect;

	@Nullable
	@Override
	public GameType cc$getGameTypeEffect() {
		return cc$gameTypeEffect;
	}

	@Override
	public void cc$setGameTypeEffect(GameType gameTypeEffect) {
		this.cc$gameTypeEffect = gameTypeEffect;
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	void onReadAdditionalSaveData(ValueInput tag, CallbackInfo ci) {
		cc$gameTypeEffect = tag
			.getString(Components.GAME_TYPE_EFFECT)
			.map(name -> GameType.byName(name, null))
			.orElse(null);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	void onAddAdditionalSaveData(ValueOutput tag, CallbackInfo ci) {
		if (cc$gameTypeEffect != null)
			tag.putString(Components.GAME_TYPE_EFFECT, cc$gameTypeEffect.getName());
	}

	@Inject(method = "tick", at = @At("HEAD"))
	void onTick(CallbackInfo ci) {
		//noinspection ConstantConditions
		ServerPlayer thiss = (ServerPlayer) (Object) this;
		UUID uuid = thiss.getUUID();

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

	@WrapOperation(
			method = "restoreFrom",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
	)
	private boolean restoreFromRedirectKeepInventory(GameRules gameRules, GameRules.Key<BooleanValue> key, Operation<Boolean> original) {
		return keepInventoryRedirect(this, original.call(gameRules, key), key);
	}

	@Inject(method = "die", at = @At("HEAD"), cancellable = true)
	private void callDeathEvent(final DamageSource source, final CallbackInfo ci) {
		EntityUtil.handleDie(this, source, ci);
	}

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void callDamageEvent(ServerLevel serverLevel, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		EntityUtil.handleDamage(this, source, amount, cir);
	}
}
