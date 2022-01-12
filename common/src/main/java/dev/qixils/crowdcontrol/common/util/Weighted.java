package dev.qixils.crowdcontrol.common.util;

/**
 * Classes which have a weight. Used to randomly select an element from a list of elements.
 * Higher weights have a higher chance of being selected.
 */
public interface Weighted {
	/**
	 * Gets the weight of this element.
	 *
	 * @return weight
	 */
	int getWeight();
}
