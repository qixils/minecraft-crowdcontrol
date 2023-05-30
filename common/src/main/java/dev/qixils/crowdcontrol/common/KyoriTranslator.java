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
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public final class KyoriTranslator extends TranslatableComponentRenderer<Locale> implements TranslationRegistry {
	private static final Logger logger = LoggerFactory.getLogger("KyoriTranslator");
	private final String prefix;
	private final TranslationRegistry translator;
	private final ClassLoader pluginClassLoader;

	/**
	 * Creates a new translator with the given language file prefix.
	 * For a prefix like "i18n/MyModName", the translator will load all files matching "i18n/MyModName_*.properties",
	 * where the asterisk is a language code like "en_US".
	 *
	 * @param modId the ID of your mod/plugin
	 * @param prefix the prefix for language resource files
	 * @param pluginClass the main class of your plugin
	 * @param locales the locales to load (used only if reflection fails)
	 */
	public KyoriTranslator(@NotNull String modId, @NotNull String prefix, @NotNull Class<?> pluginClass, @NotNull Locale @NotNull ... locales) {
		this.prefix = prefix;
		this.pluginClassLoader = pluginClass.getClassLoader();

		Pattern filePattern = Pattern.compile("^/?" + Pattern.quote(prefix) + "_");
		logger.info("Registering translator");

		// create translator
		Key name = Key.key(modId, "translations");
		translator = TranslationRegistry.create(name);
		translator.defaultLocale(Objects.requireNonNull(Translator.parseLocale("en_US")));

		// load locales
		// TODO(reflections): workaround Forge's bizarre custom "modjar" URL on Sponge 8
		String[] pkg = prefix.split("/");
		pkg = Arrays.copyOfRange(pkg, 0, pkg.length - 1);
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.addClassLoaders(pluginClassLoader)
				.addScanners(Scanners.Resources)
				.forPackage(String.join(".", pkg)));
		Set<String> resources = new HashSet<>(reflections.getResources(".+\\.properties"));
		resources.removeIf(s -> !filePattern.matcher(s).find()); // ^ reflections regex doesn't actually work

		boolean loaded = false;
		if (!resources.isEmpty()) {
			try {
				logger.info("Using Reflections to load locales");
				resources.forEach(this::register);
				loaded = true;
			} catch (Exception e) {
				logger.warn("Failed to load locales with Reflections", e);
			}
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
		ResourceBundle bundle = ResourceBundle.getBundle(prefix.replace('/', '.'), locale, pluginClassLoader, UTF8ResourceBundleControl.get());
		translator.registerAll(locale, bundle, false);
		logger.info("Registered locale " + locale);
	}

	private void register(String file) {
		logger.debug("Processing " + file);
		String[] seg1 = file.split("/");
		String[] seg2 = seg1[seg1.length - 1].split("_", 2);
		if (seg2.length <= 1)
			return;
		String[] prefSeg = prefix.split("/");
		if (!seg2[0].equals(prefSeg[prefSeg.length - 1]))
			return;
		if (!seg2[1].endsWith(".properties"))
			return;
		String localeStr = seg2[1].replace(".properties", "");
		Locale locale = Translator.parseLocale(localeStr);
		if (locale == null)
			return;
		register(locale);
	}

	@Override
	public @Nullable Component translate(final @NotNull TranslatableComponent component, final @NotNull Locale context) {
		if (translate(component.key(), context) == null) return null;
		return renderTranslatable(component, context);
	}

	@Override
	public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
		logger.debug("Plainly translating " + key + " for " + locale);
		return translator.translate(key, locale);
	}

	@Override
	protected @NotNull Component renderTranslatable(@NotNull TranslatableComponent component, @NotNull Locale context) {
		logger.debug("Richly translating " + component.key() + " for " + context);
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
