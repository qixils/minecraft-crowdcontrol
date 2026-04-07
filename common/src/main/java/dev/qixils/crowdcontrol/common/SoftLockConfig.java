package dev.qixils.crowdcontrol.common;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * The configuration for the soft-lock observer.
 */
public class SoftLockConfig {

	public static final int DEF_PERIOD = 60;
	public static final int DEF_DEATHS = 5;
	public static final int DEF_SEARCH_HORIZ = 20;
	public static final int DEF_SEARCH_VERT = 8;

	private final int period;
	private transient final Duration periodDuration;
	private final int deaths;
	private final int searchHoriz;
	private final int searchVert;

	/**
	 * Creates a new soft-lock config.
	 */
	public SoftLockConfig() {
		this(DEF_PERIOD, DEF_DEATHS, DEF_SEARCH_HORIZ, DEF_SEARCH_VERT);
	}

	/**
	 * Creates a new soft-lock config.
	 *
	 * @param period the monitoring period, in seconds
	 * @param deaths the death count threshold
	 */
	public SoftLockConfig(int period, int deaths, int searchHoriz, int searchVert) {
		this.period = period;
		this.periodDuration = Duration.ofSeconds(period);
		this.deaths = deaths;
		this.searchHoriz = searchHoriz;
		this.searchVert = searchVert;
	}

	/**
	 * How long the monitoring period is in seconds.
	 *
	 * @return monitoring period in seconds
	 */
	public int getPeriod() {
		return period;
	}

	/**
	 * How long the monitoring period is.
	 *
	 * @return monitoring period
	 */
	@NotNull
	public Duration getPeriodDuration() {
		return periodDuration;
	}

	/**
	 * How many deaths must be counted within the monitoring period to trigger the fail-safes.
	 *
	 * @return death threshold
	 */
	public int getDeaths() {
		return deaths;
	}

	/**
	 * The horizontal entity search radius.
	 *
	 * @return horizontal search radius
	 */
	public int getSearchH() {
		return searchHoriz;
	}

	/**
	 * The vertical entity search radius.
	 *
	 * @return vertical search radius
	 */
	public int getSearchV() {
		return searchVert;
	}
}
