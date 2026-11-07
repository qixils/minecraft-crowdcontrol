package dev.qixils.crowdcontrol.plugin.fabric.client.vote;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Read-only Twitch chat client based on Twitch4j (MIT).
 * Start with {@link #join(String)}, end with {@link #leave()}.
 */
public final class TwitchChat {
	private static final Logger logger = LoggerFactory.getLogger("CrowdControl/TwitchChat");
	private static final URI GATEWAY = URI.create("wss://irc-ws.chat.twitch.tv:443");
	private static final long RECONNECT_DELAY_SECONDS = 5;

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "CrowdControl-TwitchChat");
		thread.setDaemon(true);
		return thread;
	});
	private final BiConsumer<String, String> listener;
	private @Nullable String channel;
	private @Nullable Connection connection;

	/**
	 * @param listener receives the sender's user ID (or login, if unavailable) and the message
	 */
	public TwitchChat(@NotNull BiConsumer<String, String> listener) {
		this.listener = listener;
	}

	public synchronized @Nullable String getChannel() {
		return channel;
	}

	public synchronized void join(@NotNull String channel) {
		channel = channel.toLowerCase(Locale.ROOT).replaceFirst("^#", "");
		if (channel.equals(this.channel)) return;
		leave();
		this.channel = channel;
		connect();
	}

	public synchronized void leave() {
		channel = null;
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	private synchronized void connect() {
		if (channel == null) return;
		connection = new Connection(channel);
		connection.connect();
	}

	private synchronized void scheduleReconnect(@NotNull Connection closed) {
		if (closed != connection || channel == null) return;
		executor.schedule(() -> {
			synchronized (this) {
				if (closed == connection) connect();
			}
		}, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
	}

	private final class Connection extends WebSocketClient {
		private final String channel;

		private Connection(String channel) {
			super(GATEWAY);
			this.channel = channel;
		}

		@Override
		public void onOpen(ServerHandshake handshake) {
			// anonymous read-only login
			send("CAP REQ :twitch.tv/tags");
			send("NICK justinfan" + ThreadLocalRandom.current().nextInt(10000, 99999));
			send("JOIN #" + channel);
			logger.info("Joined Twitch chat for {}", channel);
		}

		@Override
		public void onMessage(String message) {
			for (String line : message.split("\r\n")) {
				if (line.isEmpty()) continue;
				if (line.startsWith("PING")) {
					send("PONG" + line.substring(4));
					continue;
				}
				handleLine(line);
			}
		}

		private void handleLine(String line) {
			// @tags :login!login@login.tmi.twitch.tv PRIVMSG #channel :message
			String tags = "";
			if (line.startsWith("@")) {
				int space = line.indexOf(' ');
				if (space == -1) return;
				tags = line.substring(1, space);
				line = line.substring(space + 1);
			}
			if (!line.startsWith(":")) return;
			int command = line.indexOf(" PRIVMSG ");
			if (command == -1) return;
			int text = line.indexOf(" :", command);
			if (text == -1) return;

			String sender = null;
			for (String tag : tags.split(";")) {
				if (tag.startsWith("user-id=")) {
					sender = tag.substring("user-id=".length());
					break;
				}
			}
			if (sender == null || sender.isEmpty()) {
				int bang = line.indexOf('!');
				sender = line.substring(1, bang == -1 ? command : bang);
			}

			try {
				listener.accept(sender, line.substring(text + 2));
			} catch (Exception e) {
				logger.warn("Failed to handle chat message", e);
			}
		}

		@Override
		public void onClose(int code, String reason, boolean remote) {
			logger.info("Disconnected from Twitch chat for {} ({} {})", channel, code, reason);
			scheduleReconnect(this);
		}

		@Override
		public void onError(Exception ex) {
			logger.warn("Twitch chat connection error", ex);
		}
	}
}
