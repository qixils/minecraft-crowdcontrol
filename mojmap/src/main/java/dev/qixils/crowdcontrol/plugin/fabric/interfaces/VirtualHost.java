package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import org.jetbrains.annotations.Nullable;

/**
 * Stores the address a client used to reach this server.
 */
public interface VirtualHost {

	/**
	 * Gets the hostname the client connected to.
	 *
	 * @return hostname, or null if the handshake didn't provide a usable one
	 */
	default @Nullable String cc$getVirtualHost() {
		return null;
	}

	/**
	 * Remembers the hostname the client connected to.
	 *
	 * @param host hostname, or null if unknown
	 */
	default void cc$setVirtualHost(@Nullable String host) {

	}

}
