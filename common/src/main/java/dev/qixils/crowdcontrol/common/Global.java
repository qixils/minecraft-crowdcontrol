package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.socket.Request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Indicates that a command can only be executed if
 * {@link Command#isGlobalCommandUsable(List, Request) global commands are usable}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Global {
}
