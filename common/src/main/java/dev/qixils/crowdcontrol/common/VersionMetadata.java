package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.common.util.SemVer;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class VersionMetadata {

	/**
	 * The version of the Minecraft server.
	 */
	private final @NotNull String minecraftVersion;

	/**
	 * The expected server mod loader.
	 */
	private final @NotNull String modLoaderExpected;

	/**
	 * The actual server mod loader.
	 */
	private final @NotNull String modLoaderActual;

	/**
	 * The version of the mod loader.
	 */
	private final @Nullable String modLoaderVersion;

	/**
	 * The version of the mod.
	 *
	 * @return mod version
	 */
	@NotNull
	public String modVersion() {
		return SemVer.MOD_STRING.split("[-+]")[0];
	}

	/**
	 * The packet to send as the server version data.
	 *
	 * @return packet body
	 */
	public Object[] packet() {
		return new Object[] {
			modVersion(),
			minecraftVersion,
			modLoaderExpected,
			modLoaderActual,
			modLoaderVersion
		};
	}
}
