package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.socket.Request;

import java.lang.annotation.*;
import java.util.List;

/**
 * Indicates that a command can only be executed if
 * {@link Command#isGlobalCommandUsable(List, Request) global commands are usable}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Global {
}
