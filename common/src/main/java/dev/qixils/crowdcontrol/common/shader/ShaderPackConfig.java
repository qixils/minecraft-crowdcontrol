package dev.qixils.crowdcontrol.common.shader;

/**
 * Configuration for offering the {@link ShaderPack}.
 *
 * @param enabled  whether to offer the pack to clients without the mod at all
 * @param required whether declining the pack should disconnect the client
 */
public record ShaderPackConfig(
	boolean enabled,
	boolean required
) {

	public static final ShaderPackConfig DEFAULT = new ShaderPackConfig(true, false);
}
