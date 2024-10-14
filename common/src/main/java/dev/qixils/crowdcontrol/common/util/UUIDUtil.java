package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public class UUIDUtil {

	/**
	 * Utility method for converting a string to a UUID.
	 * The string may be in the standard UUID format, or it may omit the dashes
	 * (i.e. {@code d1de9ca878f64aae87a18c112f675f12} is interpreted as {@code d1de9ca8-78f6-4aae-87a1-8c112f675f12}).
	 *
	 * @param uuid the string to convert
	 * @return the UUID represented by the string, or null if invalid
	 */
	@CheckReturnValue
	@Nullable
	public static UUID parseUUID(@NotNull String uuid) {
		if (uuid.length() == 32) {
			// Add dashes to the string
			uuid = uuid.substring(0, 8) + "-" +
					uuid.substring(8, 12) + "-" +
					uuid.substring(12, 16) + "-" +
					uuid.substring(16, 20) + "-" +
					uuid.substring(20);
		}

		try {
			return UUID.fromString(uuid);
		} catch (IllegalArgumentException e) {
			// Invalid UUID string
			return null;
		}
	}

	@CheckReturnValue
	@NotNull
	public static String formatUUID(UUID uuid) {
		return uuid.toString().toLowerCase(Locale.ENGLISH).replace("-", "");
	}
}
