package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

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

		// load locales
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.addScanners(Scanners.Resources)
				.forPackage("i18n"));
		for (String file : reflections.getResources(Pattern.compile(".+\\.properties"))) {
			logger.debug("Processing " + file);
			String[] segments = file.split("_", 2);
			if (segments.length <= 1)
				continue;
			if (!segments[0].equals("i18n/CrowdControl"))
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
}
