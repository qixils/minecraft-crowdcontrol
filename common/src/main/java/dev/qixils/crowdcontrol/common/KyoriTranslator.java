package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: remove sponge cope

public final class KyoriTranslator {
	private static final Logger logger = LoggerFactory.getLogger("CrowdControl/KyoriTranslator");
	private final String prefix;
	private final KyoriStoreAccessor intermediate;
	private final MiniMessageTranslationStore translator;
	private final ClassLoader pluginClassLoader;

	private static @Nullable URL loadFirst(String path, ClassLoader... classLoaders) {
		for (ClassLoader classLoader : classLoaders) {
			URL url = classLoader.getResource(path);
			if (url != null) return url;
		}

		return null;
	}

	// Find all the items in the JVM resource path
	private List<String> listResourcesIn(Plugin<?, ?> plugin, String path) {
		@Nullable URL url = loadFirst(path, pluginClassLoader, ClassLoader.getSystemClassLoader());
		@Nullable String urlPath = url == null ? null : url.getPath();

		if (url != null && urlPath != null && url.getProtocol().equals("file")) { // OS dir
			try {
				try (Stream<Path> paths = Files.list(Paths.get(new URI(urlPath)))) {
					List<String> outPaths = paths.map(file -> file.getFileName().toString()).collect(Collectors.toList());
					if (!outPaths.isEmpty()) return outPaths;
				}
			} catch (Exception ignored) {}
		}

		// packed in jar?
		if (urlPath != null) {
			try {
				int idx = urlPath.indexOf("!");
				File jarUrl = idx == -1
					? new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
					: new File(urlPath.substring("file:".length(), urlPath.indexOf("!")));
				try (JarFile jar = new JarFile(jarUrl)) {
					List<String> outPaths = Collections.list(jar.entries()).stream()
						.filter(entry -> entry.getName().startsWith(path))
						.map(entry -> entry.getName().substring(path.length()))
						.collect(Collectors.toList());
					if (!outPaths.isEmpty()) return outPaths;
				}
			} catch (Exception ignored) {}
		}

		// let's try native operations
		try {
			Path folder = plugin.getPath(path);
			if (folder == null) throw new RuntimeException("folder was null");

			try (Stream<Path> paths = Files.list(folder)) {
				List<String> outPaths = paths.map(file -> file.getFileName().toString()).collect(Collectors.toList());
				if (!outPaths.isEmpty()) return outPaths;
			}
		} catch (Exception ignored) {
		}

		try {
			try (Stream<String> paths = plugin.getPathNames(path)) {
				List<String> outPaths = paths.collect(Collectors.toList());
				if (!outPaths.isEmpty()) return outPaths;
			}
		} catch (Exception ignored) {
		}

		return Collections.emptyList();
	}

	/**
	 * Creates a new translator with the given language file prefix.
	 * For a prefix like "MyModName", the translator will load all files matching "/i18n/MyModName_*.properties",
	 * where the asterisk is a language code like "en_US".
	 *
	 * @param modId   the ID of your mod/plugin
	 * @param prefix  the prefix for language resource files
	 * @param plugin  the plugin
	 * @param locales the locales to load (used only if reflection fails)
	 */
	public KyoriTranslator(@NotNull String modId, @NotNull String prefix, @NotNull Plugin<?, ?> plugin, @NotNull Locale @NotNull ... locales) {
		this.prefix = prefix;
		this.pluginClassLoader = plugin.getClass().getClassLoader();

		Pattern filePattern = Pattern.compile(Pattern.quote(prefix) + "_(?<languageTag>\\w{2}(?:_\\w+)*)\\.properties");
		logger.debug("Registering translator");

		// create translator
		intermediate = new KyoriStoreAccessor(Key.key(modId, "intermediate_translations"));
		intermediate.defaultLocale(Objects.requireNonNull(Translator.parseLocale("en_US")));

		translator = MiniMessageTranslationStore.create(Key.key(modId, "translations"));
		translator.defaultLocale(Objects.requireNonNull(Translator.parseLocale("en_US")));

		// load locales
		boolean loaded = false;
		try {
			List<String> filenames = listResourcesIn(plugin, "i18n/");
			if (!filenames.isEmpty()) {
				for (String filename : filenames) {
					Matcher matcher = filePattern.matcher(filename);
					if (!matcher.find()) continue;

					// tag from filename (e.g. en)
					String languageTag = matcher.group("languageTag").replace('_', '-');
					Locale locale = Locale.forLanguageTag(languageTag);
					register(locale);

					loaded = true;
				}
				logger.info("Loaded locales dynamically");
			}
		} catch (Exception e) {
			loaded = false;
			logger.warn("Failed to load locales dynamically", e);
		}

		if (!loaded) {
			logger.info("Manually loading locales");
			for (Locale locale : locales)
				register(locale);
		}
	}

	private void register(Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle("i18n." + prefix, locale, pluginClassLoader);
		intermediate.registerAll(locale, bundle, false);
		for (String key : bundle.keySet()) {
			String value = intermediate.getTranslationString(key, locale);
			if (value == null) continue; // !?

			translator.register(key, locale, value.replaceAll("<(\\d+)>", "<arg:$1>"));
		}

		logger.info("Registered locale {}", locale);
	}

	public MiniMessageTranslationStore getTranslator() {
		return translator;
	}
}
