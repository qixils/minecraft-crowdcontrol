package dev.qixils.crowdcontrol.plugin.sponge7.utils;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.TextUtilImpl;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.text.translation.Translation;

public class SpongeTextUtil extends TextUtilImpl {
	public SpongeTextUtil() {
		super(null);
	}

	@NotNull
	public static Component getFixedName(org.spongepowered.api.text.translation.@NotNull Translatable translatable) {
		if (translatable.equals(EntityTypes.FURNACE_MINECART))
			return Component.translatable("cc.entity.furnace_minecart.name");
		if (translatable.equals(EntityTypes.TNT_MINECART))
			return Component.translatable("cc.entity.tnt_minecart.name");
		if (translatable.equals(EntityTypes.RIDEABLE_MINECART))
			return Component.translatable("cc.entity.rideable_minecart.name");
		if (translatable.equals(EntityTypes.LIGHTNING))
			return Component.translatable("cc.entity.lightning.name");
		return Component.translatable(translatable.getTranslation().getId());
	}

	@Override
	public @NotNull String translate(@NotNull Translatable translatable) {
		String key = translatable.translationKey();
		return Sponge.getRegistry().getTranslationById(key).map(Translation::get).orElse(key);
	}

	@Nullable
	public static Key asKey(CatalogType type) {
		try {
			return Key.key(type.getId());
		} catch (Exception e) {
			return null;
		}
	}

	@NotNull
	public static String valueOf(CatalogType type) {
		Key key = asKey(type);
		if (key == null)
			return type.getId();
		return key.value();
	}

	/**
	 * Returns the ID of an object according to the C# file.
	 *
	 * @param type typed object
	 * @return its CS file ID
	 */
	@NotNull
	public static String csIdOf(CatalogType type) {
		Key key = asKey(type);
		if (key == null)
			return type.getId();
		return CommandConstants.csIdOf(key);
	}
}
