package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class KyoriTranslator {
	private KyoriTranslator() {
	}

	private static final Logger logger = LoggerFactory.getLogger(KyoriTranslator.class);
	private static volatile boolean initialized = false;

	public static synchronized void initialize() {
		if (initialized) return;
		else initialized = true;

		// create translator
		TranslationRegistry translator = TranslationRegistry.create(Key.key("crowd-control", "translations"));
		translator.defaultLocale(Locale.US);

		// find locale path
		URL url = KyoriTranslator.class.getResource("/i18n/");
		File dir = getFileFromURL(url);
		if (dir == null)
			throw new IllegalStateException("Could not load language files (directory does not exist)");
		File[] files = dir.listFiles();
		if (files == null)
			throw new IllegalStateException("Could not load language files (path is not a directory)");

		// register locales
		for (File file : files) {
			String name = file.getName();
			logger.debug("Processing " + name);
			String[] segments = name.split("_", 2);
			if (segments.length <= 1)
				continue;
			if (!segments[0].equals("CrowdControl"))
				continue;
			if (!segments[1].endsWith(".properties"))
				continue;
			String localeStr = segments[1].replace(".properties", "");
			Locale locale = Translator.parseLocale(localeStr);
			if (locale == null)
				continue;
			ResourceBundle bundle = ResourceBundle.getBundle("i18n.CrowdControl", locale, UTF8ResourceBundleControl.get());
			translator.registerAll(locale, bundle, false);
			logger.info("Registered locale " + locale);
		}

		// register translator
		GlobalTranslator.translator().addSource(translator);
		logger.info("Registered translator");
	}

	private static @Nullable File getFileFromURL(@Nullable URL url) {
		if (url == null)
			return null;

		File file;
		try {
			file = new File(url.toURI());
		} catch (URISyntaxException e) {
			file = new File(url.getPath());
		}
		if (!file.exists())
			return null;
		return file;
	}
}
