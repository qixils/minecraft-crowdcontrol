package dev.qixils.crowdcontrol.common.shader;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Serves a single {@link ShaderPack} over HTTP.
 * There is no packet to just directly download a shader pack so we have to hack it onto a tiny web server instead.
 */
public final class ShaderPackServer implements AutoCloseable {

	private static final int STOP_DELAY_SECONDS = 1;

	private final HttpServer server;
	private final ShaderPack pack;
	private final String path;

	private ShaderPackServer(HttpServer server, ShaderPack pack, String path) {
		this.server = server;
		this.pack = pack;
		this.path = path;
	}

	// static constructor feels a little more natural since it auto starts the http server
	/**
	 * Starts a server hosting the given pack.
	 *
	 * @param pack        pack to serve
	 * @param bindAddress address to bind to, or null/empty for all interfaces
	 * @param port        port to bind to; 0 picks an arbitrary free port
	 * @return the started server
	 * @throws IOException if the socket could not be bound
	 */
	public static @NotNull ShaderPackServer start(@NotNull ShaderPack pack, @Nullable String bindAddress, int port) throws IOException {
		InetSocketAddress address = bindAddress == null || bindAddress.isBlank()
			? new InetSocketAddress(port)
			: new InetSocketAddress(bindAddress.trim(), port);

		HttpServer server = HttpServer.create(address, 0);
		String path = "/" + pack.fileName();
		ShaderPackServer packServer = new ShaderPackServer(server, pack, path);
		server.createContext("/", packServer::handle);
		server.setExecutor(Executors.newCachedThreadPool(daemonThreadFactory()));
		server.start();
		return packServer;
	}

	/**
	 * The port the server is listening on.
	 *
	 * @return bound port
	 */
	public int port() {
		return server.getAddress().getPort();
	}

	/**
	 * The path the pack is served under, including the leading slash.
	 *
	 * @return url path
	 */
	public @NotNull String path() {
		return path;
	}

	private void handle(HttpExchange exchange) throws IOException {
		try (exchange) {
			String method = exchange.getRequestMethod();
			boolean head = "HEAD".equals(method);
			if (!head && !"GET".equals(method)) {
				exchange.sendResponseHeaders(405, -1);
				return;
			}

			if (!path.equals(exchange.getRequestURI().getPath())) {
				exchange.sendResponseHeaders(404, -1);
				return;
			}

			byte[] data = pack.data();
			Headers headers = exchange.getResponseHeaders();
			headers.set("Content-Type", "application/zip");
			// the file name embeds the pack hash, so the contents at this URL can never change
			headers.set("Cache-Control", "public, max-age=31536000, immutable");

			if (head) {
				headers.set("Content-Length", Integer.toString(data.length));
				exchange.sendResponseHeaders(200, -1);
				return;
			}

			exchange.sendResponseHeaders(200, data.length);
			try (OutputStream body = exchange.getResponseBody()) {
				body.write(data);
			}
		}
	}

	@Override
	public void close() {
		server.stop(STOP_DELAY_SECONDS);
	}

	private static ThreadFactory daemonThreadFactory() {
		AtomicInteger counter = new AtomicInteger();
		return runnable -> {
			Thread thread = new Thread(runnable, "CrowdControl Shader Pack Server #" + counter.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		};
	}
}
