package dev.qixils.crowdcontrol.common.packets;

import dev.qixils.crowdcontrol.common.packets.util.Utf8String;
import dev.qixils.crowdcontrol.common.util.SemVer;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static dev.qixils.crowdcontrol.common.Plugin.VERSION_RESPONSE_KEY;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class VersionResponsePacketC2S implements PluginPacket {
	public static final Metadata<VersionResponsePacketC2S> METADATA = new Metadata<>(VERSION_RESPONSE_KEY.asString(), VersionResponsePacketC2S::new);
	protected static final int SIZE = 32;

	private final SemVer version;

	public VersionResponsePacketC2S(ByteBuf buf) {
		version = new SemVer(Utf8String.read(buf, SIZE));
	}

	@Override
	public void write(ByteBuf buf) {
		Utf8String.write(buf, version.toString(), SIZE);
	}

	@Override
	public Metadata<?> metadata() {
		return METADATA;
	}
}
