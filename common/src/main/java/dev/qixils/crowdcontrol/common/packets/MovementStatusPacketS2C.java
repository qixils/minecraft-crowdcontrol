package dev.qixils.crowdcontrol.common.packets;

import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.packets.util.Utf8String;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static dev.qixils.crowdcontrol.common.Plugin.MOVEMENT_STATUS_KEY;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class MovementStatusPacketS2C implements PluginPacket {
	public static final Metadata<MovementStatusPacketS2C> METADATA = new Metadata<>(MOVEMENT_STATUS_KEY.asString(), MovementStatusPacketS2C::new);
	protected static final int SIZE = 20;

	private final @Nullable MovementStatusType statusType;
	private final @Nullable MovementStatusValue statusValue;

	public MovementStatusPacketS2C(ByteBuf buf) {
		MovementStatusType _type = null;
		MovementStatusValue _value = null;
		try {
			_type = MovementStatusType.valueOf(Utf8String.read(buf, SIZE));
			_value = MovementStatusValue.valueOf(Utf8String.read(buf, SIZE));
		} catch (Exception ignored) {
			LOGGER.warn("Unknown type/value for MovementStatus ({}, {})", _type, _value);
		} finally {
			statusType = _type;
			statusValue = _value;
		}
	}

	@Override
	public void write(ByteBuf buf) {
		Utf8String.write(buf, Optional.ofNullable(statusType).map(Enum::name).orElse(""), SIZE);
		Utf8String.write(buf, Optional.ofNullable(statusValue).map(Enum::name).orElse(""), SIZE);
	}

	@Override
	public Metadata<?> metadata() {
		return METADATA;
	}
}
