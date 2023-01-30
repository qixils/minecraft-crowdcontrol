package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

/**
 * Represents a semantic version.
 */
public final class SemVer implements Comparable<SemVer> {

	/**
	 * The current version of the Minecraft mod.
	 */
	@NotNull
	public static final SemVer MOD;

	/**
	 * The current version of the Minecraft mod as a string.
	 */
	@NotNull
	public static final String MOD_STRING;

	/**
	 * A blank semantic version.
	 */
	@NotNull
	public static final SemVer ZERO = new SemVer(0, 0, 0);

	static {
		// load version from resources (mccc-version.txt)
		InputStream inputStream = SemVer.class.getClassLoader().getResourceAsStream("mccc-version.txt");
		if (inputStream == null)
			throw new IllegalStateException("Could not load version from resources");
		MOD_STRING = new Scanner(inputStream).next();
		MOD = new SemVer(MOD_STRING);
	}

	private final int major;
	private final int minor;
	private final int patch;

	/**
	 * Creates a new semantic version.
	 *
	 * @param major major version
	 * @param minor minor version
	 * @param patch patch version
	 */
	public SemVer(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	/**
	 * Creates a new semantic version from a string.
	 *
	 * @param version version string
	 */
	public SemVer(@NotNull String version) {
		String[] parts = version.split("\\.");
		if (parts.length < 1 || parts.length > 3)
			throw new IllegalArgumentException("Invalid version string: " + version);
		try {
			major = Integer.parseInt(parts[0]);
			minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
			patch = parts.length > 2 ? Integer.parseInt(parts[2].substring(0, parts[2].indexOf('-'))) : 0;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid version string: " + version);
		}
	}

	/**
	 * Returns the major version.
	 *
	 * @return major version
	 */
	public int major() {
		return major;
	}

	/**
	 * Returns the minor version.
	 *
	 * @return minor version
	 */
	public int minor() {
		return minor;
	}

	/**
	 * Returns the patch version.
	 *
	 * @return patch version
	 */
	public int patch() {
		return patch;
	}

	/**
	 * Returns the version as a string.
	 *
	 * @return version string
	 */
	@Override
	public String toString() {
		return major + "." + minor + "." + patch;
	}

	/**
	 * Returns whether this version is greater than or equal to the given version.
	 *
	 * @param other other version
	 * @return whether this version is greater than or equal to the given version
	 */
	public boolean isAtLeast(@NotNull SemVer other) {
		return compareTo(other) >= 0;
	}

	/**
	 * Returns whether this version is less than or equal to the given version.
	 *
	 * @param other other version
	 * @return whether this version is less than or equal to the given version
	 */
	public boolean isAtMost(@NotNull SemVer other) {
		return compareTo(other) <= 0;
	}

	/**
	 * Returns whether this version is greater than the given version.
	 *
	 * @param other other version
	 * @return whether this version is greater than the given version
	 */
	public boolean isGreaterThan(@NotNull SemVer other) {
		return compareTo(other) > 0;
	}

	/**
	 * Returns whether this version is less than the given version.
	 *
	 * @param other other version
	 * @return whether this version is less than the given version
	 */
	public boolean isLessThan(@NotNull SemVer other) {
		return compareTo(other) < 0;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SemVer other = (SemVer) o;
		return major == other.major && minor == other.minor && patch == other.patch;
	}

	@Override
	public int hashCode() {
		return Objects.hash(major, minor, patch);
	}

	@Override
	public int compareTo(@NotNull SemVer o) {
		if (major != o.major)
			return major - o.major;
		if (minor != o.minor)
			return minor - o.minor;
		return patch - o.patch;
	}
}
