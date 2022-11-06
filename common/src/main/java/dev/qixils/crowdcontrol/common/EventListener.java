package dev.qixils.crowdcontrol.common;

import java.lang.annotation.*;

/**
 * Indicates that a command listens for events dispatched by the Minecraft server API.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventListener {
}
