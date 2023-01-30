package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;

/**
 * An object whose availability is version-dependent.
 */
public interface Versioned {

	/**
	 * The version that this object was added in.
	 *
	 * @return semantic version
	 */
	@NotNull SemVer addedIn();
}
