package dev.qixils.crowdcontrol.common.packets;

import dev.qixils.crowdcontrol.common.packets.util.BitMaskUtil;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Set;

import static dev.qixils.crowdcontrol.common.Plugin.EXTRA_FEATURE_KEY;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class ExtraFeaturePacketC2S implements PluginPacket {
	public static final Metadata<ExtraFeaturePacketC2S> METADATA = new Metadata<>(EXTRA_FEATURE_KEY.asString(), ExtraFeaturePacketC2S::new);

	private final Set<ExtraFeature> features;

	public ExtraFeaturePacketC2S(ByteBuf buf) {
		features = BitMaskUtil.fromBitMask(ExtraFeature.class, buf.readLong());
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeLong(BitMaskUtil.toBitMask(features));
	}

	@Override
	public Metadata<?> metadata() {
		return METADATA;
	}
}
