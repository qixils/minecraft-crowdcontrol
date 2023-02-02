package dev.qixils.crowdcontrol.plugin.sponge7.utils;

import dev.qixils.crowdcontrol.common.util.TextUtilImpl;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.NotNull;
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
		if (translatable.equals(EntityTypes.LIGHTNING))
			return Component.translatable("cc.entity.lightning.name");
		return Component.translatable(translatable.getTranslation().getId());
	}

	@Override
	public @NotNull String translate(@NotNull Translatable translatable) {
		String key = translatable.translationKey();
		return Sponge.getRegistry().getTranslationById(key).map(Translation::get).orElse(key);
	}

	@NotNull
	public static Key asKey(CatalogType type) {
		return Key.key(type.getId());
	}

	@NotNull
	public static String valueOf(CatalogType type) {
		return asKey(type).value();
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
		String value = key.value();

		if (!key.namespace().equals(Key.MINECRAFT_NAMESPACE))
			return value;

		switch (value) {
			case "villager_golem":
				return "iron_golem";
			case "web":
				return "cobweb";
			case "mooshroom":
				return "mushroom_cow";
			case "tnt":
				return "primed_tnt";
			case "zombie_pigman":
				return "zombified_piglin";
			case "illusion_illager":
				return "illusioner";
			case "vindication_illager":
				return "vindicator";
			case "sweeping":
				return "sweeping_edge";
			case "chest_minecart":
				return "minecart_chest";
			case "furnace_minecart":
				return "minecart_furnace";
			case "hopper_minecart":
				return "minecart_hopper";
			case "tnt_minecart":
				return "minecart_tnt";
			default:
				return value;
		}
	}
}
