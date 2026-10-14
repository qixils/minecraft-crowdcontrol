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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds the client resource pack containing the {@link Shader#bundled() bundled} post effects.
 * <p>
 * This runs at build time (see {@code common/build.gradle.kts}) and isn't shipped;
 * the plugin only gets to know the result through {@link ShaderPack}.
 */
public final class ShaderPackBuilder {

	/**
	 * Clients cache packs by hash so we insert a fixed timestamp on every file to ensure consistency between builds.
	 */
	private static final FileTime EPOCH = FileTime.fromMillis(0);

	private static final Pattern INCLUDE_PATTERN = Pattern.compile("^\\s*#include\\s*<([^>]+)>\\s*$", Pattern.MULTILINE);

	private ShaderPackBuilder() {
	}

	/**
	 * Writes the pack to a file.
	 *
	 * @param args the file to write, then the client resource pack format to declare
	 * @throws IOException if a referenced asset is missing or the file can't be written
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2)
			throw new IllegalArgumentException("Usage: ShaderPackBuilder <output file> <resource pack format>");
		Path output = Path.of(args[0]).toAbsolutePath();
		Files.createDirectories(output.getParent());
		Files.write(output, build(Integer.parseInt(args[1])));
	}

	/**
	 * Builds the pack from the post effect assets on the classpath.
	 *
	 * @param packFormat the client resource pack format of the targeted game version
	 * @return zip contents
	 * @throws IOException if a referenced asset is missing or unreadable
	 */
	public static byte @NotNull [] build(int packFormat) throws IOException {
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

		return zip(files);
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
		try (InputStream stream = ShaderPackBuilder.class.getClassLoader().getResourceAsStream(path)) {
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
}
