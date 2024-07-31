package dev.qixils.crowdcontrol.common.packets;

import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Duration;

import static dev.qixils.crowdcontrol.common.Plugin.SET_LANGUAGE_KEY;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class SetLanguagePacketS2C implements PluginPacket {
	public static final Metadata<SetLanguagePacketS2C> METADATA = new Metadata<>(SET_LANGUAGE_KEY.asString(), SetLanguagePacketS2C::new);

	private final LanguageState state;
	private final Duration duration;

	public SetLanguagePacketS2C(ByteBuf buf) {
		state = LanguageState.values()[buf.readInt()];
		duration = Duration.ofMillis(buf.readLong());
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(state.ordinal());
		buf.writeLong(duration.toMillis());
	}

	@Override
	public Metadata<?> metadata() {
		return METADATA;
	}
}
