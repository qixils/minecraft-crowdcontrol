package dev.qixils.crowdcontrol.common.util;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract implementation of the utility class for working with Kyori Adventure objects.
 */
@Getter
@Accessors(fluent = true)
public class TextUtilImpl implements TextUtil {
	private final @Nullable ComponentFlattener flattener;
	private final @NotNull PlainTextComponentSerializer serializer;

	/**
	 * Creates a new TextUtil given a {@link ComponentFlattener}.
	 *
	 * @param flattener component flattener
	 */
	public TextUtilImpl(@Nullable ComponentFlattener flattener) {
		this.flattener = flattener;
		this.serializer = flattener == null
				? PlainTextComponentSerializer.plainText() // uses impl's default flattener
				: PlainTextComponentSerializer.builder().flattener(flattener).build();
	}
}
