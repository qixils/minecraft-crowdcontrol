package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.AbstractTranslationStore;
import org.jspecify.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;

public class KyoriStoreAccessor extends AbstractTranslationStore.StringBased<String> {
	/**
	 * Creates a new abstract, string-based translation store with a given name.
	 *
	 * @param name the name
	 * @since 4.20.0
	 */
	protected KyoriStoreAccessor(Key name) {
		super(name);
	}

	@Override
	protected String parse(String string, Locale locale) {
		return string;
	}

	@Override
	public @Nullable MessageFormat translate(String key, Locale locale) {
		return null;
	}

	public String getTranslationString(String string, Locale locale) {
		return this.translationValue(string, locale);
	}
}
