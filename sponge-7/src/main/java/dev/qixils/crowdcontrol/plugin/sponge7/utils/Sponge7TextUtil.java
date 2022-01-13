package dev.qixils.crowdcontrol.plugin.sponge7.utils;

import dev.qixils.crowdcontrol.common.util.TextUtil;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.translation.Translation;

public class Sponge7TextUtil extends TextUtil {
	public Sponge7TextUtil() {
		super(ComponentFlattener.basic());
	}

	@Override
	public @NotNull String translate(@NotNull Translatable translatable) {
		String key = translatable.translationKey();
		return Sponge.getRegistry().getTranslationById(key).map(Translation::get).orElse(key);
	}

	public static String valueOf(CatalogType type) {
		return type.getId().replaceFirst("minecraft:", "");
	}
}
