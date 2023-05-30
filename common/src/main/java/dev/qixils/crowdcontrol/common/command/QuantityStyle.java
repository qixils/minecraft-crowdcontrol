package dev.qixils.crowdcontrol.common.command;

/**
 * The style with which to render the quantity used in an effect.
 */
public enum QuantityStyle {
	/**
	 * The quantity is not rendered.
	 */
	NONE,
	/**
	 * The quantity is rendered by appending its component as an argument.
	 */
	APPEND,
	/**
	 * The quantity is rendered by appending its component as an argument and suffixing the ID of the effect name in the
	 * translatable key with "_x", i.e. {@code cc.effect.take_item.name} becomes {@code cc.effect.take_item_x.name}.
	 */
	APPEND_X,
	/**
	 * The quantity is rendered by prepending its component as an argument.
	 */
	PREPEND,
	/**
	 * The quantity is rendered by prepending its component as an argument and suffixing the ID of the effect name in
	 * the translatable key with "_x", i.e. {@code cc.effect.take_item.name} becomes {@code cc.effect.take_item_x.name}.
	 */
	PREPEND_X,
}
