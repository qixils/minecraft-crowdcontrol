package dev.qixils.crowdcontrol;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A boolean value that may be unknown.
 *
 * @since 3.3.0
 */
@ApiStatus.AvailableSince("3.3.0")
public enum TriState {
	/**
	 * The value is {@code true}.
	 *
	 * @since 3.3.0
	 */
	@ApiStatus.AvailableSince("3.3.0")
	TRUE(true),
	/**
	 * The value is {@code false}.
	 *
	 * @since 3.3.0
	 */
	@ApiStatus.AvailableSince("3.3.0")
	FALSE(false),
	/**
	 * The value is unknown.
	 *
	 * @since 3.3.0
	 */
	@ApiStatus.AvailableSince("3.3.0")
	UNKNOWN(null);

	private final Boolean value;

	TriState(Boolean value) {
		this.value = value;
	}

	/**
	 * Returns the {@link TriState} equivalent of the given {@link Boolean}.
	 *
	 * @param value the {@link Boolean} to convert
	 * @return the equivalent {@link TriState}
	 * @since 3.3.0
	 */
	@ApiStatus.AvailableSince("3.3.0")
	@NotNull
	public static TriState fromBoolean(@Nullable Boolean value) {
		if (value == null)
			return UNKNOWN;
		else if (value)
			return TRUE;
		else
			return FALSE;
	}

	/**
	 * Returns the {@link Boolean} equivalent of this {@code TriState}.
	 *
	 * @return boolean equivalent
	 * @since 3.3.0
	 */
	@ApiStatus.AvailableSince("3.3.0")
	@Nullable
	public Boolean getBoolean() {
		return value;
	}

	/**
	 * Returns the primitive boolean equivalent of this {@code TriState}.
	 *
	 * @return primitive boolean equivalent
	 * @since 3.3.0
	 */
	@ApiStatus.AvailableSince("3.3.0")
	public boolean getPrimitiveBoolean() {
		return value != null && value;
	}
}
