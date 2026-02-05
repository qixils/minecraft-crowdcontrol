package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
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
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

// TODO: remove sponge cope

public final class KyoriTranslator extends TranslatableComponentRenderer<Locale> implements TranslationRegistry {
	private static final Logger logger = LoggerFactory.getLogger("CrowdControl/KyoriTranslator");
	private final String prefix;
	private final TranslationRegistry translator;
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
		Key name = Key.key(modId, "translations");
		translator = TranslationRegistry.create(name);
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

	@Override
	public @NotNull Key name() {
		return translator.name();
	}

	@Override
	public boolean contains(@NotNull String key) {
		return translator.contains(key);
	}

	@Override
	public boolean contains(@NotNull String key, @NotNull Locale locale) {
		return translator.contains(key, locale);
	}

	@Override
	public void defaultLocale(@NotNull Locale locale) {
		translator.defaultLocale(locale);
	}

	@Override
	public void register(@NotNull String key, @NotNull Locale locale, @NotNull MessageFormat format) {
		translator.register(key, locale, format);
	}

	@Override
	public void unregister(@NotNull String key) {
		translator.unregister(key);
	}

	private void register(Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle("i18n." + prefix, locale, pluginClassLoader);
		translator.registerAll(locale, bundle, false);
		logger.info("Registered locale {}", locale);
	}

	@Override
	public @Nullable Component translate(final @NotNull TranslatableComponent component, final @NotNull Locale context) {
		if (translate(component.key(), context) == null) return null;
		return renderTranslatable(component, context);
	}

	@Override
	public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
		logger.debug("Plainly translating {} for {}", key, locale);
		return translator.translate(key, locale);
	}

	@Override
	protected @NotNull Component renderTranslatable(@NotNull TranslatableComponent component, @NotNull Locale context) {
		logger.debug("Richly translating {} for {}", component.key(), context);
		final @Nullable MessageFormat format = translate(component.key(), context);

		// this probably shouldn't cause a stack overflow because of the top-level check for null in the #translate method
		//  (and in fact, it hasn't from a lot of testing)
		// also this needs to be here because #optionallyRenderChildrenAppendAndBuild calls this method to, well, render children
		// although that's not to say that this couldn't be improved. it probably could be.
		if (format == null)
			return GlobalTranslator.renderer().render(component, context);

		final TextComponent.Builder builder = Component.text(); // mostly just a dummy for appending children
		this.mergeStyle(component, builder, context);

		final List<Component> args = component.args();
		if (args.isEmpty()) {
			// no arguments makes this render very simple
			builder.append(miniMessage().deserialize(format.format(null, new StringBuffer(), null).toString()));
		} else {
			final TagResolver.Builder resolver = TagResolver.builder();
			for (int i = 0; i < args.size(); i++)
				resolver.tag(String.valueOf(i), Tag.selfClosingInserting(this.render(args.get(i), context)));
			builder.append(miniMessage().deserialize(format.format(null, new StringBuffer(), null).toString(), resolver.build()));
		}
		return this.optionallyRenderChildrenAppendAndBuild(component.children(), builder, context);
	}
}
