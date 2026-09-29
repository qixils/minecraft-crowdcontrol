package dev.qixils.crowdcontrol.common.shader;

import org.jetbrains.annotations.NotNull;

/**
 * Configuration for hosting and offering the {@link ShaderPack}.
 *
 * @param enabled       whether to offer the pack to clients without the mod at all
 * @param port          port for the built-in HTTP server; 0 picks an arbitrary free port
 * @param bindAddress   address the HTTP server binds to; blank binds all interfaces
 * @param publicAddress host name clients should download the pack from; blank derives it from the
 *                      address the client used to reach the Minecraft server
 * @param url           complete URL of an externally hosted copy of the pack; blank uses the
 *                      built-in server. Only useful if the hosted file is byte-identical to the
 *                      generated one, since the hash is validated by the client
 * @param required      whether declining the pack should disconnect the client
 */
public record ShaderPackConfig(
	boolean enabled,
	int port,
	@NotNull String bindAddress,
	@NotNull String publicAddress,
	@NotNull String url,
	boolean required
) {

	public static final int DEFAULT_PORT = 24051;

	public static final ShaderPackConfig DEFAULT = new ShaderPackConfig(true, DEFAULT_PORT, "", "", "", false);
}
