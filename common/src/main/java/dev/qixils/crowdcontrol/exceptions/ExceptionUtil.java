package dev.qixils.crowdcontrol.exceptions;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Utility class containing common methods relating to exceptions.
 *
 * @since 3.3.0
 */
@ApiStatus.AvailableSince("3.3.0")
public class ExceptionUtil {
	private ExceptionUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Helper method for determining if a provided exception class is part of an exception's stacktrace.
	 *
	 * @param potentialCause class to search for in stacktrace
	 * @param exception      exception to be searched
	 * @return true if the exception class is found
	 * @since 3.3.0
	 */
	@ApiStatus.AvailableSince("3.3.0")
	public static boolean isCause(@NotNull Class<? extends Throwable> potentialCause, @Nullable Throwable exception) {
		validateNotNull(potentialCause, "potentialCause");
		if (exception == null) return false;
		if (potentialCause.isInstance(exception)) return true;
		// TODO: handle AggregatedIOException
		return isCause(potentialCause, exception.getCause());
	}

	/**
	 * Validates that the provided object is not null.
	 *
	 * @param object object to validate
	 * @param <T>    type of object to accept and return
	 * @return the object if not null
	 * @throws IllegalArgumentException if the object is null
	 * @since 3.3.0
	 */
	@Contract("null -> fail; !null -> !null")
	@NotNull
	@ApiStatus.AvailableSince("3.3.0")
	public static <T> T validateNotNull(@Nullable T object) throws IllegalArgumentException {
		return validateNotNull(object, null);
	}

	/**
	 * Validates that the provided object is not null.
	 *
	 * @param object       object to validate
	 * @param variableName name of the variable being validated
	 * @param <T>          type of object to accept and return
	 * @return the object if not null
	 * @throws IllegalArgumentException if the object is null
	 * @since 3.3.0
	 */
	@Contract("null, _ -> fail; !null, _ -> !null")
	@NotNull
	@ApiStatus.AvailableSince("3.3.0")
	public static <T> T validateNotNull(@Nullable T object, @Nullable String variableName) throws IllegalArgumentException {
		if (object == null)
			throw new IllegalArgumentException(validateNotNullElse(variableName, "Object") + " cannot be null");
		return object;
	}

	/**
	 * Returns the first parameter if it is not null, else the second parameter.
	 *
	 * @param object1 object to check for nullness
	 * @param object2 backup object to return instead
	 * @param <T>     type of object to accept and return
	 * @return the first object if not null, else the second
	 * @throws IllegalArgumentException if the second parameter is null
	 * @since 3.3.1
	 */
	@Contract("!null, _ -> param1; null, _ -> param2")
	@NotNull
	@ApiStatus.AvailableSince("3.3.1")
	public static <T> T validateNotNullElse(@Nullable T object1, @NotNull T object2) {
		if (object1 != null) {
			return object1;
		}
		return validateNotNull(object2);
	}

	/**
	 * Returns the first parameter if it is not null, else the object provided by the second
	 * parameter.
	 *
	 * @param object   object to check for nullness
	 * @param provider provider of a backup object to return instead
	 * @param <T>      type of object to accept and return
	 * @return the first object if not null, else the object provided by the supplier
	 * @throws IllegalArgumentException if the object provided by the second parameter is null
	 * @since 3.3.1
	 */
	@Contract("!null, _ -> param1")
	@NotNull
	@ApiStatus.AvailableSince("3.3.1")
	public static <T> T validateNotNullElseGet(@Nullable T object, @NotNull Supplier<@NotNull T> provider) {
		if (object != null) {
			return object;
		}
		return validateNotNull(validateNotNull(provider).get());
	}
}
