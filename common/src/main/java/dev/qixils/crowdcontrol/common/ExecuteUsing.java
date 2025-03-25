package dev.qixils.crowdcontrol.common;


import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Deprecated // maybe can remove this later but definitely important we're aware of it in the conversion
@ApiStatus.ScheduledForRemoval(inVersion = "4.0.0") // ditto
public @interface ExecuteUsing {

	Type value();

	enum Type {
		ASYNC,
		SYNC_GLOBAL,
	}
}
