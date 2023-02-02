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

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KyoriTranslator {
	private KyoriTranslator() {
	}

	private static final Set<Locale> HARDCODED_LOCALES = Stream.of(
			"en_US"
	)
			.map(Translator::parseLocale)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	private static final Logger logger = LoggerFactory.getLogger(KyoriTranslator.class);
	private static volatile boolean initialized = false;

	private static void register(Locale locale, TranslationRegistry translator, @Nullable ClassLoader classLoader) {
		if (classLoader == null)
			classLoader = KyoriTranslator.class.getClassLoader();
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.CrowdControl", locale, classLoader, UTF8ResourceBundleControl.get());
		translator.registerAll(locale, bundle, false);
		logger.info("Registered locale " + locale);
	}

	private static void register(String file, TranslationRegistry translator, @Nullable ClassLoader classLoader) {
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
		register(locale, translator, classLoader);
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

	private static boolean tryFileRegister(Iterable<String> directories, Iterable<ClassLoader> classLoaders, TranslationRegistry translator) {
		for (String directory : directories) {
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
						register(file.getName(), translator, classLoader);
					return true;
				} catch (Exception e) {
					logger.debug("Could not load language files", e);
				}
			}
		}
		return false;
	}

	private static boolean tryStreamRegister(Iterable<String> directories, Iterable<ClassLoader> classLoaders, TranslationRegistry translator) {
		for (String directory : directories) {
			for (ClassLoader classLoader : classLoaders) {
				try {
					List<String> files = getResourceFiles(directory, classLoader);
					for (String file : files)
						register(file, translator, classLoader);
					return true;
				} catch (Exception e) {
					logger.debug("Could not load language files", e);
				}
			}
		}
		return false;
	}

	private static boolean tryPathRegister(Iterable<String> directories, Iterable<ClassLoader> classLoaders, TranslationRegistry translator) {
		for (String directory : directories) {
			for (ClassLoader classLoader : classLoaders) {
				try {
					URL url = classLoader.getResource(directory);
					Path dir = getPathFromURL(url);
					if (dir == null)
						throw new IllegalStateException("Could not load language files (directory does not exist)");
					try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
						for (Path file : stream)
							register(file.getFileName().toString(), translator, classLoader);
					}
					return true;
				} catch (Exception ignored) {
					// logger.debug("Could not load language files");
				}
			}
		}
		return false;
	}

	public static synchronized void initialize(ClassLoader... secondaryClassLoaders) {
		if (initialized) return;
		else initialized = true;

		logger.info("Registering translator");

		// create translator
		TranslationRegistry translator = TranslationRegistry.create(Key.key("crowd-control", "translations"));
		translator.defaultLocale(Locale.US);

		// create class loaders list
		List<String> directories = Arrays.asList("/i18n/", "i18n/");
		List<ClassLoader> classLoaders = new ArrayList<>(secondaryClassLoaders.length + 3);
		classLoaders.add(KyoriTranslator.class.getClassLoader());
		classLoaders.add(ClassLoader.getSystemClassLoader());
		classLoaders.add(Thread.currentThread().getContextClassLoader());
		classLoaders.addAll(Arrays.asList(secondaryClassLoaders));

		// load locales
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.addClassLoaders(classLoaders.toArray(new ClassLoader[0]))
				.addScanners(Scanners.Resources)
				.forPackage("i18n"));
		Set<String> resources = reflections.getResources(Pattern.compile("i18n/.+\\.properties"));

		// try one of a million redundancies to load locales
		// (this is probably over the top and i'm sure a lot of them aren't necessary and will never be used but. uh. oh well lol)
		if (!resources.isEmpty()) {
			logger.info("Using Reflections to load locales");
			resources.forEach(file -> register(file, translator, null));
		} else if (tryFileRegister(directories, classLoaders, translator)) {
			logger.info("Used File method to load locales");
		} else if (tryStreamRegister(directories, classLoaders, translator)) {
			logger.info("Used InputStream method to load locales");
		} else if (tryPathRegister(directories, classLoaders, translator)) {
			logger.info("Used Path method to load locales");
		} else {
			// TODO: try ClassGraph
			logger.info("Manually loading locales");
			for (Locale locale : HARDCODED_LOCALES)
				register(locale, translator, null);
		}

		// register translator
		GlobalTranslator.translator().addSource(translator);
		logger.info("Registered translator");
	}
}
