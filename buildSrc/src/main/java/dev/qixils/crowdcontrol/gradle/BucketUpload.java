package dev.qixils.crowdcontrol.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Uploads a file to an S3-compatible bucket, such as Cloudflare R2,
 * then checks that it can be downloaded back from its public URL.
 * Uploads are never overwritten.
 */
public abstract class BucketUpload extends DefaultTask {

	private static final DateTimeFormatter AMZ_DATE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
	private static final int DOWNLOAD_ATTEMPTS = 5;
	private static final Duration DOWNLOAD_RETRY_DELAY = Duration.ofSeconds(3);

	/**
	 * The file to upload.
	 */
	@InputFile
	public abstract RegularFileProperty getFile();

	/**
	 * Where the file will be publicly downloadable from.
	 * Its path is used as the object key, as with an R2 custom domain or {@code r2.dev} URL.
	 */
	@Input
	public abstract Property<String> getPublicUrl();

	/**
	 * MIME type to serve the file with.
	 */
	@Input
	public abstract Property<String> getContentType();

	/**
	 * S3 API endpoint, e.g. {@code https://<account id>.r2.cloudflarestorage.com}.
	 */
	@Input
	public abstract Property<String> getEndpoint();

	/**
	 * Name of the bucket to upload into.
	 */
	@Input
	public abstract Property<String> getBucket();

	/**
	 * Region of the bucket; {@code auto} for R2.
	 */
	@Input
	public abstract Property<String> getRegion();

	@Internal
	public abstract Property<String> getAccessKeyId();

	@Internal
	public abstract Property<String> getSecretAccessKey();

	@TaskAction
	public void upload() throws IOException, InterruptedException {
		byte[] content = Files.readAllBytes(getFile().get().getAsFile().toPath());
		URI publicUrl = URI.create(required(getPublicUrl(), "public URL"));
		String key = publicUrl.getPath().replaceFirst("^/+", "");
		if (key.isEmpty() || key.endsWith("/"))
			throw new GradleException("The public URL " + publicUrl + " does not point to a file");

		String endpoint = required(getEndpoint(), "endpoint").replaceFirst("/+$", "");
		String path = "/" + SigV4.encode(required(getBucket(), "bucket"), false) + "/" + SigV4.encode(key, true);
		URI objectUrl = URI.create(endpoint + path);

		// no outputs are declared, so this always runs and checks the bucket's actual state
		HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
		int existing = send(http, "HEAD", objectUrl, path, null).statusCode();
		if (existing == 200) {
			getLogger().lifecycle("{} was already uploaded", publicUrl);
		} else if (existing == 404) {
			HttpResponse<String> put = send(http, "PUT", objectUrl, path, content);
			if (put.statusCode() / 100 != 2)
				throw new GradleException("Could not upload " + key + " (HTTP " + put.statusCode() + "): " + put.body());
			getLogger().lifecycle("Uploaded {}", publicUrl);
		} else {
			throw new GradleException("Could not check whether " + key + " exists (HTTP " + existing + "); are the credentials correct?");
		}

		verifyDownload(http, publicUrl, content);
	}

	private HttpResponse<String> send(HttpClient http, String method, URI url, String path, byte[] body) throws IOException, InterruptedException {
		String payloadHash = body == null ? SigV4.EMPTY_HASH : SigV4.sha256Hex(body);

		SortedMap<String, String> headers = new TreeMap<>();
		headers.put("host", url.getPort() == -1 ? url.getHost() : url.getHost() + ':' + url.getPort());
		headers.put("x-amz-content-sha256", payloadHash);
		headers.put("x-amz-date", AMZ_DATE.format(ZonedDateTime.now(ZoneOffset.UTC)));
		if (body != null) {
			headers.put("content-type", getContentType().get());
			// the name identifies the content, so it can be cached forever
			headers.put("cache-control", "public, max-age=31536000, immutable");
		}
		String authorization = SigV4.authorization(method, path, headers, payloadHash,
			required(getRegion(), "region"), required(getAccessKeyId(), "access key ID"), required(getSecretAccessKey(), "secret access key"));

		HttpRequest.Builder request = HttpRequest.newBuilder(url)
			.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body))
			.header("authorization", authorization);
		// the client adds the host header itself
		headers.forEach((name, value) -> {
			if (!name.equals("host")) request.header(name, value);
		});
		return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
	}

	private void verifyDownload(HttpClient http, URI publicUrl, byte[] expected) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(publicUrl).header("user-agent", "minecraft-crowdcontrol build").GET().build();
		int status = 0;
		for (int attempt = 1; attempt <= DOWNLOAD_ATTEMPTS; attempt++) {
			if (attempt > 1) Thread.sleep(DOWNLOAD_RETRY_DELAY.toMillis());
			HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
			status = response.statusCode();
			if (status != 200) continue;
			if (!Arrays.equals(response.body(), expected))
				throw new GradleException(publicUrl + " serves different content to what was uploaded");
			return;
		}
		throw new GradleException(publicUrl + " could not be downloaded (HTTP " + status + "); is the bucket publicly accessible at that address?");
	}

	private static String required(Property<String> property, String name) {
		String value = property.getOrElse("").trim();
		if (value.isEmpty())
			throw new GradleException("No bucket " + name + " was configured for uploading");
		return value;
	}
}
