package dev.qixils.crowdcontrol.common.shader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.impl.Shader;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A client resource pack containing the {@link Shader#bundled() bundled} post effects.
 */
public final class ShaderPack {

	/**
	 * Clients cache packs by hash so we insert a fixed timestamp on every file to ensure consistency between restarts.
	 */
	private static final FileTime EPOCH = FileTime.fromMillis(0);

	private static final Pattern INCLUDE_PATTERN = Pattern.compile("^\\s*#include\\s*<([^>]+)>\\s*$", Pattern.MULTILINE);

	private final byte[] data;
	private final String hash;
	private final UUID id;

	private ShaderPack(byte[] data, String hash, UUID id) {
		this.data = data;
		this.hash = hash;
		this.id = id;
	}

	/**
	 * The zipped pack.
	 *
	 * @return zip contents
	 */
	public byte @NotNull [] data() {
		return data;
	}

	/**
	 * The SHA-1 of {@link #data()}, as expected by the resource pack protocol.
	 *
	 * @return lowercase hex digest
	 */
	public @NotNull String hash() {
		return hash;
	}

	/**
	 * A stable ID derived from {@link #hash()}, used to add and remove the pack client-side.
	 *
	 * @return pack id
	 */
	public @NotNull UUID id() {
		return id;
	}

	/**
	 * The file name to serve the pack under.
	 *
	 * @return file name
	 */
	public @NotNull String fileName() {
		// include the hash so it can be appropriately cached by clients
		return "crowdcontrol-shaders-" + hash + ".zip";
	}

	/**
	 * Builds the pack from the post effect assets on the classpath.
	 *
	 * @param packFormat the client resource pack format of the running game version
	 * @return the built pack
	 * @throws IOException if a referenced asset is missing or unreadable
	 */
	public static @NotNull ShaderPack build(int packFormat) throws IOException {
		// sorted so that zip entry order (and therefore the hash) is deterministic
		Map<String, byte[]> files = new TreeMap<>();

		for (Shader shader : Shader.values()) {
			if (!shader.bundled()) continue;
			String chainPath = "assets/" + Plugin.NAMESPACE + "/post_effect/" + shader.getPath() + ".json";
			byte[] chain = read(chainPath);
			files.put(chainPath, chain);
			for (String program : referencedPrograms(chain))
				addProgram(files, program);
		}

		if (files.isEmpty())
			throw new IOException("No bundled post effects were found on the classpath");

		files.put("pack.mcmeta", mcmeta(packFormat));

		byte[] zipped = zip(files);
		String hash = sha1(zipped);
		return new ShaderPack(zipped, hash, UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.US_ASCII)));
	}

	private static boolean isOurs(Key key) { return Plugin.NAMESPACE.equals(key.namespace()); }

	/**
	 * Adds a shader program plus anything it {@code #include}s from our own namespace.
	 */
	private static void addProgram(Map<String, byte[]> files, String path) throws IOException {
		if (files.containsKey(path)) return;
		byte[] source = read(path);
		files.put(path, source);

		Matcher matcher = INCLUDE_PATTERN.matcher(new String(source, StandardCharsets.UTF_8));
		while (matcher.find()) {
			Key include = Key.key(matcher.group(1));
			if (!isOurs(include)) continue; // vanilla includes already exist client-side
			addProgram(files, "assets/" + include.namespace() + "/shaders/include/" + include.value());
		}
	}

	/**
	 * Collects the {@code crowdcontrol}-namespaced shader programs a post effect chain refers to.
	 */
	private static Set<String> referencedPrograms(byte[] chain) throws IOException {
		JsonNode root = new ObjectMapper().readTree(chain);
		JsonNode passes = root.path("passes");
		if (!passes.isArray())
			return Collections.emptySet();

		Set<String> programs = new LinkedHashSet<>();
		for (JsonNode pass : passes) {
			collectProgram(programs, pass.path("vertex_shader").asText(null), ".vsh");
			collectProgram(programs, pass.path("fragment_shader").asText(null), ".fsh");
		}
		return programs;
	}

	private static void collectProgram(Set<String> programs, String rawId, String extension) {
		if (rawId == null || rawId.isEmpty()) return;
		Key id = Key.key(rawId);
		if (!isOurs(id)) return; // shipped with the game
		programs.add("assets/" + id.namespace() + "/shaders/" + id.value() + extension);
	}

	private static byte[] mcmeta(int packFormat) {
		// see net.minecraft.server.packs.metadata.pack.PackFormat
		String json = "{\n"
			+ "    \"pack\": {\n"
			+ "        \"description\": \"Crowd Control screen effects\",\n"
			+ "        \"min_format\": " + packFormat + ",\n"
			+ "        \"max_format\": " + packFormat + "\n"
			+ "    }\n"
			+ "}\n";
		return json.getBytes(StandardCharsets.UTF_8);
	}

	private static byte[] read(String path) throws IOException {
		try (InputStream stream = ShaderPack.class.getClassLoader().getResourceAsStream(path)) {
			if (stream == null)
				throw new IOException("Missing bundled shader asset: " + path);
			return stream.readAllBytes();
		}
	}

	private static byte[] zip(Map<String, byte[]> files) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
			// STORED keeps the output independent of the JDK's deflate implementation, which keeps
			// the hash stable across Java versions
			zip.setMethod(ZipOutputStream.STORED);
			for (Map.Entry<String, byte[]> file : files.entrySet()) {
				byte[] content = file.getValue();
				CRC32 crc = new CRC32();
				crc.update(content);

				ZipEntry entry = new ZipEntry(file.getKey());
				entry.setSize(content.length);
				entry.setCompressedSize(content.length);
				entry.setCrc(crc.getValue());
				entry.setCreationTime(EPOCH);
				entry.setLastModifiedTime(EPOCH);
				entry.setLastAccessTime(EPOCH);

				zip.putNextEntry(entry);
				zip.write(content);
				zip.closeEntry();
			}
		}
		return bytes.toByteArray();
	}

	private static String sha1(byte[] data) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-1 is unavailable", e);
		}
		StringBuilder hex = new StringBuilder(40);
		for (byte b : digest.digest(data))
			hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
		return hex.toString();
	}
}
