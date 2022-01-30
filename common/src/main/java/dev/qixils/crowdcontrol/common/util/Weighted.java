package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.Range;

/**
 * A class which has a weight. Used to randomly select an element from a list of elements.
 * Higher weights have a higher chance of being selected.
 */
public interface Weighted {
	/**
	 * Gets the weight of this element.
	 * Higher weights have a higher chance of being selected from a random draw.
	 *
	 * @return positive integer
	 */
	@Range(from = 1, to =Long.MAX_VALUE)
	int getWeight();
}
