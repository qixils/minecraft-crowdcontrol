package dev.qixils.crowdcontrol.plugin.sponge7.utils;

import dev.qixils.crowdcontrol.common.util.TextUtil;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.translation.Translatable;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.translation.Translation;

public class Sponge7TextUtil extends TextUtil {
	public Sponge7TextUtil() {
		super(ComponentFlattener.basic());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param translatable object to translate
	 * @return translated string
	 * @deprecated does not work on Sponge API v7
	 */
	@Override
	@Deprecated
	public String translate(Translatable translatable) {
		String key = translatable.translationKey();
		return Sponge.getRegistry().getTranslationById(key).map(Translation::get).orElse(key);
	}

	public static String valueOf(CatalogType type) {
		return type.getId().replaceFirst("minecraft:", "");
	}
}
