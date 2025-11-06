package dev.qixils.crowdcontrol.common;

/**
 * The configuration for custom effects.
 */
public final class CustomEffectsConfig {
	private final boolean enabled;

	/**
	 * Constructs a new custom effects configuration.
	 *
	 * @param enabled whether the whole feature is enabled
	 */
	public CustomEffectsConfig(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Constructs an empty limit configuration.
	 */
	public CustomEffectsConfig() {
		this(false);
	}

	/**
	 * Whether custom effects are enabled.
	 *
	 * @return whether custom effects are enabled
	 */
	public boolean enabled() {
		return enabled;
	}

	@Override
	public String toString() {
		return "CustomEffectsConfig{" +
			"enabled=" + enabled +
//			", abc=" + abc +
			'}';
	}
}
