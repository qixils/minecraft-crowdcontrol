package dev.qixils.crowdcontrol.plugin.utils;

import dev.qixils.crowdcontrol.common.util.TextUtil;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.translation.Translatable;

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
		// TODO: this is possible to support
		throw new UnsupportedOperationException("Translation services are unavailable in Sponge API v7");
	}
}
