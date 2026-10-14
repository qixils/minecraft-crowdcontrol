package dev.qixils.crowdcontrol.gradle;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * Just enough of AWS Signature Version 4 to talk to an S3-compatible bucket.
 *
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-header-based-auth.html">AWS documentation</a>
 */
public final class SigV4 {

	/**
	 * The payload hash of an empty request body.
	 */
	public static final String EMPTY_HASH = sha256Hex(new byte[0]);

	private SigV4() {
	}

	/**
	 * Calculates the {@code Authorization} header for a request.
	 *
	 * @param method          HTTP method
	 * @param canonicalUri    request path exactly as sent, already {@linkplain #encode(String, boolean) encoded}
	 * @param headers         every header to sign, keyed by lowercase name; must include {@code host},
	 *                        {@code x-amz-date} and {@code x-amz-content-sha256}
	 * @param payloadHash     {@link #sha256Hex(byte[]) SHA-256} of the request body
	 * @param region          bucket region; {@code auto} for Cloudflare R2
	 * @param accessKeyId     access key ID
	 * @param secretAccessKey secret access key
	 * @return header value
	 */
	public static String authorization(String method, String canonicalUri, SortedMap<String, String> headers, String payloadHash,
									   String region, String accessKeyId, String secretAccessKey) {
		String amzDate = headers.get("x-amz-date");
		String date = amzDate.substring(0, 8);
		String scope = date + "/" + region + "/s3/aws4_request";
		String signedHeaders = String.join(";", headers.keySet());

		String canonicalRequest = String.join("\n",
			method,
			canonicalUri,
			"", // no query string
			headers.entrySet().stream().map(e -> e.getKey() + ':' + e.getValue().trim() + '\n').collect(Collectors.joining()),
			signedHeaders,
			payloadHash
		);
		String stringToSign = String.join("\n",
			"AWS4-HMAC-SHA256",
			amzDate,
			scope,
			sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8))
		);

		byte[] key = hmac(("AWS4" + secretAccessKey).getBytes(StandardCharsets.UTF_8), date);
		key = hmac(key, region);
		key = hmac(key, "s3");
		key = hmac(key, "aws4_request");
		String signature = HexFormat.of().formatHex(hmac(key, stringToSign));

		return "AWS4-HMAC-SHA256 Credential=" + accessKeyId + '/' + scope
			+ ", SignedHeaders=" + signedHeaders
			+ ", Signature=" + signature;
	}

	/**
	 * Percent-encodes everything but unreserved characters, as SigV4 expects.
	 *
	 * @param value     text to encode
	 * @param keepSlash whether to leave {@code /} alone, as in paths
	 * @return encoded text
	 */
	public static String encode(String value, boolean keepSlash) {
		StringBuilder out = new StringBuilder();
		for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
			char c = (char) (b & 0xFF);
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
				|| c == '-' || c == '_' || c == '.' || c == '~' || (keepSlash && c == '/'))
				out.append(c);
			else
				out.append('%').append(HexFormat.of().withUpperCase().toHexDigits(b));
		}
		return out.toString();
	}

	/**
	 * Hashes data for use as a payload hash.
	 *
	 * @param data data to hash
	 * @return lowercase hex SHA-256
	 */
	public static String sha256Hex(byte[] data) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	private static byte[] hmac(byte[] key, String data) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(key, "HmacSHA256"));
			return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}
}
