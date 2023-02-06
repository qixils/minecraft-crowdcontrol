package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.kyori.examination.Examinable;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public final class KyoriTranslator extends AbstractComponentRenderer<Locale> implements WrappedAudienceProvider {
	private static final Set<Locale> LOCALES = Stream.of(
			"en_US"
	)
			.map(Translator::parseLocale)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	private static final Set<String> DIRECTORIES = Stream.of(
			"/i18n/",
			"i18n/"
	)
			.collect(Collectors.toSet());
	private static final Logger logger = LoggerFactory.getLogger(KyoriTranslator.class);
	private final TranslationRegistry translator;
	private final List<ClassLoader> classLoaders;
	private final Supplier<AudienceProvider> wrappedProvider;

	public KyoriTranslator(AudienceProvider wrappedProvider, ClassLoader... secondaryClassLoaders) {
		this(() -> wrappedProvider, secondaryClassLoaders);
	}

	public KyoriTranslator(Supplier<AudienceProvider> wrappedProvider, ClassLoader... secondaryClassLoaders) {
		this.wrappedProvider = wrappedProvider;
		logger.info("Registering translator");

		// create translator
		translator = TranslationRegistry.create(Key.key("crowd-control", "translations"));
		translator.defaultLocale(Objects.requireNonNull(Translator.parseLocale("en_US")));

		// create class loaders list
		classLoaders = new ArrayList<>(secondaryClassLoaders.length + 3);
		classLoaders.add(KyoriTranslator.class.getClassLoader());
		classLoaders.addAll(Arrays.asList(secondaryClassLoaders));
		classLoaders.add(Thread.currentThread().getContextClassLoader());
		classLoaders.add(ClassLoader.getSystemClassLoader());

		// load locales
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.addClassLoaders(classLoaders.toArray(new ClassLoader[0]))
				.addScanners(Scanners.Resources)
				.forPackage("i18n"));
		Set<String> resources = reflections.getResources(Pattern.compile("i18n/CrowdControl_.+\\.properties")); // TODO: test on fabric, make sure i didn't break it

		// try one of a million redundancies to load locales
		// (this is probably over the top and i'm sure a lot of them aren't necessary and will never be used but. uh. oh well lol)
		if (!resources.isEmpty()) {
			logger.info("Using Reflections to load locales");
			resources.forEach(file -> register(file, null));
		} else if (tryFileRegister()) {
			logger.info("Used File method to load locales");
		} else if (tryStreamRegister()) {
			logger.info("Used InputStream method to load locales");
		} else if (tryPathRegister()) {
			logger.info("Used Path method to load locales");
		} else {
			// TODO: try ClassGraph
			logger.info("Manually loading locales");
			for (Locale locale : LOCALES)
				register(locale, null);
		}
	}

	private int countRegisteredLocales() {
		Set<Locale> locales = new HashSet<>();
		if (!(translator instanceof Examinable))
			return -1;
		// TODO figure out how to use Examiner
		return -1;//locales.size();
	}

	private void register(Locale locale, @Nullable ClassLoader classLoader) {
		if (classLoader == null)
			classLoader = KyoriTranslator.class.getClassLoader();
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.CrowdControl", locale, classLoader, UTF8ResourceBundleControl.get());
		int registeredLocales = countRegisteredLocales();
		translator.registerAll(locale, bundle, false);

		if (registeredLocales > -1 && registeredLocales == countRegisteredLocales())
			logger.warn("Failed to register locale " + locale);
		else
			logger.info("Registered locale " + locale);
	}

	private void register(String file, @Nullable ClassLoader classLoader) {
		logger.debug("Processing " + file);
		String[] segments = file.split("_", 2);
		if (segments.length <= 1)
			return;
		if (!segments[0].equals("i18n/CrowdControl") && !segments[0].equals("CrowdControl"))
			return;
		if (!segments[1].endsWith(".properties"))
			return;
		String localeStr = segments[1].replace(".properties", "");
		Locale locale = Translator.parseLocale(localeStr);
		if (locale == null)
			return;
		register(locale, classLoader);
	}

	private static @Nullable File getFileFromURL(@Nullable URL url) {
		if (url == null)
			return null;

		try {
			File file;
			try {
				file = new File(url.toURI());
			} catch (Exception e) {
				file = new File(url.getPath());
			}
			if (file.listFiles() == null)
				return null;
			return file;
		} catch (Exception e) {
			return null;
		}
	}

	private static @Nullable Path getPathFromURL(@Nullable URL url) {
		if (url == null)
			return null;

		try {
			Path path;
			try {
				path = Paths.get(url.toURI());
			} catch (Exception e) {
				path = Paths.get(url.getPath());
			}
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
				if (stream == null)
					return null;
			}
			return path;
		} catch (Exception e) {
			return null;
		}
	}

	private static List<String> getResourceFiles(String path, ClassLoader classLoader) throws IOException {
		List<String> filenames = new ArrayList<>();

		try (InputStream in = classLoader.getResourceAsStream(path)) {
			if (in == null)
				throw new IllegalStateException("Could not load language files (directory does not exist)");
			try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
				String resource;
				while ((resource = br.readLine()) != null) {
					filenames.add(resource);
				}
			}
		}

		return filenames;
	}

	private boolean tryFileRegister() {
		for (String directory : DIRECTORIES) {
			for (ClassLoader classLoader : classLoaders) {
				try {
					URL url = classLoader.getResource(directory);
					File dir = getFileFromURL(url);
					if (dir == null)
						throw new IllegalStateException("Could not load language files (directory does not exist)");
					File[] files = dir.listFiles();
					if (files == null)
						throw new IllegalStateException("Could not load language files (path is not a directory)");
					for (File file : files)
						register(file.getName(), classLoader);
					return true;
				} catch (Exception e) {
					logger.debug("Could not load language files", e);
				}
			}
		}
		return false;
	}

	private boolean tryStreamRegister() {
		for (String directory : DIRECTORIES) {
			for (ClassLoader classLoader : classLoaders) {
				try {
					List<String> files = getResourceFiles(directory, classLoader);
					if (files.isEmpty())
						throw new IllegalStateException("Could not load language files (directory does not exist)");
					for (String file : files)
						register(file, classLoader);
					return true;
				} catch (Exception e) {
					logger.debug("Could not load language files", e);
				}
			}
		}
		return false;
	}

	private boolean tryPathRegister() {
		for (String directory : DIRECTORIES) {
			for (ClassLoader classLoader : classLoaders) {
				try {
					URL url = classLoader.getResource(directory);
					Path dir = getPathFromURL(url);
					if (dir == null)
						throw new IllegalStateException("Could not load language files (directory does not exist)");
					try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
						Iterator<Path> iterator = stream.iterator();
						if (!iterator.hasNext())
							throw new IllegalStateException("Could not load language files (directory is empty)");
						while (iterator.hasNext())
							register(iterator.next().getFileName().toString(), classLoader);
					}
					return true;
				} catch (Exception e) {
					logger.debug("Could not load language files", e);
				}
			}
		}
		return false;
	}

	@Override
	protected @NotNull Component renderTranslatable(final @NotNull TranslatableComponent component, final @NotNull Locale context) {
		if (!component.key().startsWith("cc."))
			return GlobalTranslator.renderer().render(component, context);

		final @Nullable MessageFormat format = translator.translate(component.key(), context);
		if (format == null) {
			// we don't have a translation for this component, default to super
			return super.renderTranslatable(component, context);
		}

		final List<Component> args = component.args();

		final TextComponent.Builder builder = Component.text(); // mostly just a dummy for appending children
		this.mergeStyle(component, builder, context);

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

	@Override
	public @NotNull Audience wrap(final @NotNull Audience audience) {
		return RenderedAudience.translated(audience, this);
	}

	@Override
	public @NotNull AudienceProvider provider() {
		return wrappedProvider.get();
	}

	// TODO: override ComponentFlattener?
}
