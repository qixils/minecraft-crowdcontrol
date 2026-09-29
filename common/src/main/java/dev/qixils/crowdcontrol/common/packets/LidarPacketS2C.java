package dev.qixils.crowdcontrol.common.packets;

import dev.qixils.crowdcontrol.common.util.SemVer;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Duration;

import static dev.qixils.crowdcontrol.common.Plugin.LIDAR_KEY;

/**
 * Starts or stops the client-side LiDAR effect
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class LidarPacketS2C implements PluginPacket {
	public static final Metadata<LidarPacketS2C> METADATA = new Metadata<>(LIDAR_KEY.asString(), LidarPacketS2C::new);
	public static final SemVer ADDED_IN = new SemVer("4.6.2");

	/**
	 * How long the effect should run for from the moment the packet arrives.
	 * Used only as a fallback if a stop event (duration zero) isn't received.
	 */
	private final Duration duration;

	public LidarPacketS2C(ByteBuf buf) {
		duration = Duration.ofMillis(buf.readLong());
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeLong(duration.toMillis());
	}

	@Override
	public Metadata<?> metadata() {
		return METADATA;
	}
}
