package dev.qixils.crowdcontrol.plugin.sponge7.data.entity;

import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class OriginalDisplayNameData extends AbstractSingleData
		<Text, OriginalDisplayNameData, ImmutableOriginalDisplayNameData> {

	static final int CONTENT_VERSION = 1;

	public OriginalDisplayNameData(Text value) {
		super(
				SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME,
				ExceptionUtil.validateNotNullElse(value, Text.EMPTY),
				Text.EMPTY
		);
	}

	public OriginalDisplayNameData() {
		this(Text.EMPTY);
	}

	public Value<Text> originalDisplayName() {
		return getValueGetter();
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME, getValue());
	}

	@Override
	public Optional<OriginalDisplayNameData> fill(DataHolder dataHolder, MergeFunction overlap) {
		OriginalDisplayNameData merged = overlap.merge(this, dataHolder.get(OriginalDisplayNameData.class).orElse(null));
		setValue(merged.originalDisplayName().get());

		return Optional.of(this);
	}

	@Override
	public Optional<OriginalDisplayNameData> from(DataContainer container) {
		if (container.contains(SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME)) {
			//noinspection OptionalGetWithoutIsPresent -- check is implied by the if statement
			return Optional.of(setValue(container.getObject(SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME.getQuery(), Text.class).get()));
		}
		return Optional.empty();
	}

	@Override
	public OriginalDisplayNameData copy() {
		return new OriginalDisplayNameData(getValue());
	}

	@Override
	protected Value<Text> getValueGetter() {
		return Sponge.getRegistry().getValueFactory().createValue(
				SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME,
				this.value,
				this.defaultValue
		);
	}

	@Override
	public ImmutableOriginalDisplayNameData asImmutable() {
		return new ImmutableOriginalDisplayNameData(getValue());
	}

	@Override
	public int getContentVersion() {
		return CONTENT_VERSION;
	}
}
