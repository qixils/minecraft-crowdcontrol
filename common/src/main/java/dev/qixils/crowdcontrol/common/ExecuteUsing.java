package dev.qixils.crowdcontrol.common;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ExecuteUsing {

	Type value();

	enum Type {
		ASYNC,
		SYNC_GLOBAL,
	}
}
