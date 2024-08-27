package dev.qixils.crowdcontrol.plugin.fabric.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a method is a listener for a specific event.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {
}
