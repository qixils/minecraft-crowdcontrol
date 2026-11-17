package dev.qixils.crowdcontrol.common.shader;

import dev.qixils.crowdcontrol.common.command.impl.Shader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

/**
 * The client resource pack containing the {@link Shader#bundled() bundled} post effects.
 * <p>
 * The pack is built, and for releases uploaded, alongside the plugin (see {@code common/build.gradle.kts}),
 * so all the plugin knows about it is its hash and where the official copy lives.
 *
 * @param hash SHA-1 of the zipped pack, as expected by the resource pack protocol
 * @param url  where the official copy can be downloaded from, or null if this build wasn't made for release
 */
public record ShaderPack(@NotNull String hash, @Nullable URI url) {

	private static final String RESOURCE = "mccc-shader-pack.properties";

	/**
	 * A stable ID derived from {@link #hash()}, used to add and remove the pack client-side.
	 *
	 * @return pack id
	 */
	public @NotNull UUID id() {
		return UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.US_ASCII));
	}

	/**
	 * Reads the details of the pack that was built alongside this copy of the plugin.
	 *
	 * @return pack details
	 * @throws IOException if the details are missing or malformed
	 */
	public static @NotNull ShaderPack load() throws IOException {
		Properties properties = new Properties();
		try (InputStream stream = ShaderPack.class.getClassLoader().getResourceAsStream(RESOURCE)) {
			if (stream == null)
				throw new IOException("Could not find " + RESOURCE);
			properties.load(stream);
		}

		String hash = properties.getProperty("hash", "");
		if (!hash.matches("[0-9a-f]{40}"))
			throw new IOException("Invalid shader pack hash: " + hash);

		String url = properties.getProperty("url", "");
		try {
			return new ShaderPack(hash, url.isBlank() ? null : new URI(url));
		} catch (URISyntaxException e) {
			throw new IOException("Invalid shader pack URL: " + url, e);
		}
	}
}
