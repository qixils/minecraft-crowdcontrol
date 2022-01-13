package dev.qixils.crowdcontrol.common.util;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A simpler {@link Component} builder.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@NoArgsConstructor
public class TextBuilder implements ComponentLike {
	private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
	@NotNull private Builder builder = Component.text();
	@NotNull private Builder result = Component.text();

	private boolean initialized = false;

	/**
	 * Creates a new builder with its color set
	 */
	public TextBuilder(@Nullable TextColor color) {
		color(color);
	}

	/**
	 * Creates a new builder with its color and text decorations set
	 */
	public TextBuilder(@Nullable TextColor color, @NotNull TextDecoration... decorations) {
		color(color);
		decorate(decorations);
	}

	/**
	 * Creates a new builder with its text decorations set
	 */
	public TextBuilder(@NotNull TextDecoration... decorations) {
		decorate(decorations);
	}

	/**
	 * Converts the input text to a component and appends it to the internal builder
	 * <p>Note: this does not apply any text, color, or formatting changes to the builder itself
	 */
	public TextBuilder(@Nullable String formattedText) {
		next(formattedText);
	}

	/**
	 * Creates a new builder with the same data as another.
	 */
	public TextBuilder(@Nullable TextBuilder json) {
		if (json != null) {
			builder = json.builder.build().toBuilder();
			result = json.builder.build().toBuilder();
			initialized = json.initialized;
		}
	}

	/**
	 * Converts the input to a component and appends it to the internal builder
	 * <p>Note: this does not apply any text, color, or formatting changes to the builder itself
	 */
	public TextBuilder(@Nullable ComponentLike component) {
		next(component);
	}

	/**
	 * Creates a new builder with its raw text set and the provided color applied
	 * @param rawText raw text, meaning formatting codes are ignored
	 */
	public TextBuilder(@NotNull String rawText, @NotNull TextDecoration... decorations) {
		content(rawText);
		decorate(decorations);
	}

	/**
	 * Creates a new builder with its raw text set and the provided color applied
	 * @param rawText raw text, meaning formatting codes are ignored
	 */
	public TextBuilder(@NotNull String rawText, @Nullable TextColor color) {
		content(rawText);
		color(color);
	}

	/**
	 * Creates a new builder with its raw text set, provided color applied, and text decorations set
	 * @param rawText raw text, meaning formatting codes are ignored
	 */
	public TextBuilder(@NotNull String rawText, @Nullable TextColor color, @NotNull TextDecoration... decorations) {
		content(rawText);
		color(color);
		decorate(decorations);
	}

	/**
	 * Creates a new builder with its raw text set and provided style applied
	 * @param rawText raw text, meaning formatting codes are ignored
	 * @param style style that overwrites saved colors and text decorations
	 */
	public TextBuilder(@NotNull String rawText, @NotNull Style style) {
		content(rawText);
		style(style);
	}

	/**
	 * Creates a new builder with a prefix at the start and the color set to DARK_AQUA.
	 * @param prefix prefix text
	 * @param contents component to append after the prefix, or null for nothing
	 */
	private TextBuilder(@NotNull String prefix, @Nullable ComponentLike contents) {
		color(NamedTextColor.DARK_AQUA)
				.next("[", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
				.next(prefix, NamedTextColor.YELLOW)
				.next("]", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
				.next(Component.space()).next(contents);
	}

	/**
	 * Creates a new builder with a prefix.
	 * @param prefix prefix text
	 * @return a new builder
	 */
	@Contract("_ -> new")
	public static TextBuilder fromPrefix(@NotNull String prefix) {
		return fromPrefix(prefix, (ComponentLike) null);
	}

	/**
	 * Creates a new builder with a prefix.
	 * @param prefix prefix text
	 * @param contents contents to append to the prefix
	 * @return a new builder
	 */
	@Contract("_, _ -> new")
	public static TextBuilder fromPrefix(@NotNull String prefix, @Nullable ComponentLike contents) {
		return new TextBuilder(prefix, contents);
	}

	/**
	 * Creates a new builder with a prefix. Converts the input text to a component and appends it to the internal builder
	 * <p>
	 * Note: this does not apply any text, color, or formatting changes to the builder itself
	 * @param prefix prefix text
	 * @param contents contents to append to the prefix
	 * @return a new builder
	 */
	@Contract("_, _ -> new")
	public static TextBuilder fromPrefix(@NotNull String prefix, @Nullable String contents) {
		return new TextBuilder(prefix, new TextBuilder(contents));
	}

	/**
	 * Creates a new builder formatted as an error.
	 * @param prefix prefix text
	 * @param error error to display
	 * @return a new builder
	 */
	@Contract("_, _ -> new")
	public static TextBuilder fromError(@NotNull String prefix, @NotNull ComponentLike error) {
		return new TextBuilder(prefix, error).color(NamedTextColor.RED);
	}

	/**
	 * Creates a new builder formatted as an error.
	 * @param prefix prefix text
	 * @param error error to display
	 * @return a new builder
	 */
	@Contract("_, _ -> new")
	public static TextBuilder fromError(@NotNull String prefix, @NotNull String error) {
		return new TextBuilder(prefix, Component.text(error)).color(NamedTextColor.RED);
	}

	/**
	 * Converts the input text to a colored and formatted component and appends it to the internal builder
	 *
	 * @param formattedText text formatted with ampersands or section symbols
	 * @return this builder
	 */
	@NotNull
	@Contract("_ -> this")
	public TextBuilder next(@Nullable String formattedText) {
		if (formattedText != null)
			builder.append(SERIALIZER.deserialize(formattedText));
		return this;
	}

	/**
	 * Creates a component with its text set and appends it to the internal builder
	 *
	 * @param rawText raw text, color codes are ignored
	 * @return this builder
	 */
	@NotNull
	@Contract("_ -> this")
	public TextBuilder rawNext(@Nullable String rawText) {
		if (rawText != null)
			builder.append(Component.text(rawText));
		return this;
	}

	/**
	 * Creates a component with its text and color set and appends it to the internal builder
	 *
	 * @param rawText raw text, color codes are ignored
	 * @param color   text color
	 * @return this builder
	 */
	@NotNull
	@Contract("_, _ -> this")
	public TextBuilder next(@Nullable String rawText, @Nullable TextColor color) {
		if (rawText != null)
			builder.append(Component.text(rawText, color));
		return this;
	}

	/**
	 * Creates a component with its text, color, and decorations set, and appends it to the internal builder
	 * @param rawText raw text, color codes are ignored
	 * @return this builder
	 */
	@NotNull @Contract("_, _, _ -> this")
	public TextBuilder next(@Nullable String rawText, @Nullable TextColor color, TextDecoration... decorations) {
		if (rawText != null)
			builder.append(Component.text(rawText, color, decorations));
		return this;
	}

	/**
	 * Creates a component with its text, color, and decorations set, and appends it to the internal builder
	 * @param rawText raw text, color codes are ignored
	 * @return this builder
	 */
	@NotNull @Contract("_, _ -> this")
	public TextBuilder next(@Nullable String rawText, TextDecoration... decorations) {
		if (rawText != null)
			builder.append(new TextBuilder(rawText, decorations));
		return this;
	}

	/**
	 * Sets the raw text for the base text component
	 * @param rawText raw text, color codes are ignored
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder content(@NotNull String rawText) {
		builder.content(rawText);
		return this;
	}

	/**
	 * Appends a component to the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder next(@Nullable ComponentLike component) {
		if (component != null)
			builder.append(component);
		return this;
	}

	/**
	 * Appends the current working Component to the output Component and resets the working
	 * component to default. This effectively saves the working Component and switches to
	 * building a new one.
	 * @return this builder
	 */
	@NotNull @Contract("-> this")
	public TextBuilder group() {
		result.append(builder);
		builder = Component.text();
		return this;
	}

	/**
	 * Adds a new line and creates a new {@link #group()}
	 * @return this builder
	 */
	@NotNull @Contract("-> this")
	public TextBuilder newline() {
		return newline(false);
	}

	/**
	 * Adds a new line and optionally creates a new {@link #group()}
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder newline(boolean newGroup) {
		builder.append(Component.text(System.lineSeparator()));
		if (newGroup)
			group();
		return this;
	}

	/**
	 * Creates an empty line (2x {@link #newline()})
	 */
	@NotNull @Contract("-> this")
	public TextBuilder line() {
		newline();
		newline();
		return this;
	}

	/**
	 * Sets the color for the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder color(@Nullable TextColor color) {
		builder.color(color);
		return this;
	}

	/**
	 * Enables the provided text decorations
	 * @param decorations varargs list of decorations
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder decorate(@NotNull TextDecoration... decorations) {
		return decorate(true, decorations);
	}

	/**
	 * Sets the state of text decorations
	 * @param state whether to enable or disable the decorations
	 * @param decorations varargs list of decorations
	 * @return this builder
	 */
	@NotNull @Contract("_, _ -> this")
	public TextBuilder decorate(boolean state, @NotNull TextDecoration... decorations) {
		return decorate(state, Arrays.asList(decorations));
	}

	/**
	 * Sets the state of text decorations
	 * @param state state to set the decorations to
	 * @param decorations varargs list of decorations
	 * @return this builder
	 */
	@NotNull @Contract("_, _ -> this")
	public TextBuilder decorate(@NotNull TextDecoration.State state, @NotNull TextDecoration... decorations) {
		return decorate(state, Arrays.asList(decorations));
	}

	/**
	 * Sets the state of text decorations
	 * @param state whether to enable or disable the decorations
	 * @param decorations collection of decorations
	 * @return this builder
	 */
	@NotNull @Contract("_, _ -> this")
	public TextBuilder decorate(boolean state, @NotNull Collection<TextDecoration> decorations) {
		return decorate(TextDecoration.State.byBoolean(state), new HashSet<>(decorations));
	}

	/**
	 * Sets the state of text decorations
	 * @param state state to set the decorations to
	 * @param decorations collection of decorations
	 * @return this builder
	 */
	@NotNull @Contract("_, _ -> this")
	public TextBuilder decorate(@NotNull TextDecoration.State state, @NotNull Collection<TextDecoration> decorations) {
		decorations.forEach(decoration -> builder.decoration(decoration, state));
		return this;
	}

	/**
	 * Sets the state of text decorations
	 * @param decorations map of decorations to states
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder decorate(@NotNull Map<TextDecoration, TextDecoration.State> decorations) {
		decorations.forEach((decoration, state) -> builder.decoration(decoration, state));
		return this;
	}

	/**
	 * Enables bold on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("-> this")
	public TextBuilder bold() {
		return bold(true);
	}

	/**
	 * Sets the state of bold on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder bold(boolean state) {
		return bold(TextDecoration.State.byBoolean(state));
	}

	/**
	 * Sets the state of bold on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder bold(@NotNull TextDecoration.State state) {
		return decorate(state, TextDecoration.BOLD);
	}

	/**
	 * Enables italicization on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("-> this")
	public TextBuilder italic() {
		return italic(true);
	}

	/**
	 * Sets the state of italicization on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder italic(boolean state) {
		return italic(TextDecoration.State.byBoolean(state));
	}

	/**
	 * Sets the state of italicization on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder italic(@NotNull TextDecoration.State state) {
		return decorate(state, TextDecoration.ITALIC);
	}


	/**
	 * Enables strikethrough on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("-> this")
	public TextBuilder strikethrough() {
		return strikethrough(true);
	}

	/**
	 * Sets the state of strikethrough on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder strikethrough(boolean state) {
		return strikethrough(TextDecoration.State.byBoolean(state));
	}

	/**
	 * Sets the state of strikethrough on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder strikethrough(@NotNull TextDecoration.State state) {
		return decorate(state, TextDecoration.STRIKETHROUGH);
	}

	/**
	 * Enables underlines on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("-> this")
	public TextBuilder underline() {
		return underline(true);
	}

	/**
	 * Sets the state of underlines on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder underline(boolean state) {
		return underline(TextDecoration.State.byBoolean(state));
	}

	/**
	 * Sets the state of underlines on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder underline(@NotNull TextDecoration.State state) {
		return decorate(state, TextDecoration.UNDERLINED);
	}

	/**
	 * Enables obfuscation (random gibberish text) on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("-> this")
	public TextBuilder obfuscate() {
		return obfuscate(true);
	}

	/**
	 * Sets the state of obfuscation (random gibberish text) on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder obfuscate(boolean state) {
		return obfuscate(TextDecoration.State.byBoolean(state));
	}

	/**
	 * Sets the state of obfuscation (random gibberish text) on the internal builder
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder obfuscate(@NotNull TextDecoration.State state) {
		return decorate(state, TextDecoration.OBFUSCATED);
	}

	/**
	 * Sets the style of the internal builder, replacing existing colors and decorations
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder style(@NotNull Style style) {
		builder.style(style);
		return this;
	}

	/**
	 * Sets the style of the internal builder, replacing existing colors and decorations
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder style(@NotNull Consumer<Style.Builder> style) {
		builder.style(style);
		return this;
	}

	/**
	 * Adds an action to run on click
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder clickEvent(@Nullable ClickEvent clickEvent) {
		builder.clickEvent(clickEvent);
		return this;
	}

	/**
	 * Prompts the player to open a URL when clicked
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder url(@NotNull String url) {
		return clickEvent(ClickEvent.openUrl(url));
	}

	/**
	 * Prompts the player to open a URL when clicked
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder url(@NotNull URL url) {
		return clickEvent(ClickEvent.openUrl(url));
	}

	/**
	 * Makes the player run a command when clicked
	 * @param command a command, forward slash not required
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder command(@NotNull String command) {
		if (!command.startsWith("/"))
			command = "/" + command;
		return clickEvent(ClickEvent.runCommand(command));
	}

	/**
	 * Suggests a command to a player on click by typing it into their chat window
	 * @param text some text, usually a command
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder suggest(@NotNull String text) {
		return clickEvent(ClickEvent.suggestCommand(text));
	}

	/**
	 * Copies text to the user's clipboard on click
	 * @param text text to copy
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder copy(@NotNull String text) {
		return clickEvent(ClickEvent.copyToClipboard(text));
	}

	/**
	 * Sets the page of a book when clicked
	 * @param page page number
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder bookPage(int page) {
		return bookPage(String.valueOf(page));
	}

	/**
	 * Sets the page of a book when clicked
	 * @param page page number
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder bookPage(@NotNull String page) {
		return clickEvent(ClickEvent.changePage(page));
	}

	/**
	 * Clears this builder's hover text
	 * @return this builder
	 */
	@NotNull @Contract("-> this")
	public TextBuilder hover() {
		builder.hoverEvent(null);
		return this;
	}

	/**
	 * Sets the text shown on hover
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public <V> TextBuilder hover(@Nullable HoverEventSource<V> hoverEvent) {
		builder.hoverEvent(hoverEvent);
		return this;
	}

	/**
	 * Sets a component to display on hover
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder hover(@Nullable ComponentLike component) {
		if (component != null)
			builder.hoverEvent(component.asComponent().asHoverEvent());
		return this;
	}

	/**
	 * Sets a string that will be inserted in chat when this component is shift-clicked
	 * @return this builder
	 */
	@NotNull @Contract("_ -> this")
	public TextBuilder insert(@Nullable String insertion) {
		builder.insertion(insertion);
		return this;
	}

	/**
	 * "Initializes" the builder. This does nothing on its own. See {@link #isInitialized()} for more information.
	 * @return this builder
	 */
	@NotNull @Contract("-> this")
	public TextBuilder initialize() {
		this.initialized = true;
		return this;
	}

	/**
	 * Helper boolean that does not affect anything within the builder itself.
	 * Can be used by external methods to determine if the builder has been setup.
	 * To set this boolean to true, use {@link #initialize()}
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Executes {@link #group()} and returns the final result
	 * @return resultant component
	 */
	@NotNull
	public TextComponent build() {
		group();
		return result.build();
	}

	/**
	 * Alias of {@link #build()}. Executes {@link #group()} and returns the final result
	 * @return resultant component
	 */
	@Override
	@NotNull
	public Component asComponent() {
		return build();
	}

}

