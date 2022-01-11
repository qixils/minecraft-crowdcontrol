package dev.qixils.crowdcontrol.plugin.data.entity;

import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;

import static dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME;

public class ImmutableOriginalDisplayNameData extends AbstractImmutableSingleData
		<Text, ImmutableOriginalDisplayNameData, OriginalDisplayNameData> {

	private final ImmutableValue<Text> immutableValue;

	public ImmutableOriginalDisplayNameData(Text value) {
		super(
				ORIGINAL_DISPLAY_NAME,
				ExceptionUtil.validateNotNullElse(value, Text.EMPTY),
				Text.EMPTY
		);

		this.immutableValue = Sponge.getRegistry().getValueFactory().createValue(
				ORIGINAL_DISPLAY_NAME,
				this.value,
				this.defaultValue
		).asImmutable();
	}

	public ImmutableOriginalDisplayNameData() {
		this(Text.EMPTY);
	}

	public ImmutableValue<Text> originalDisplayName() {
		return getValueGetter();
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(ORIGINAL_DISPLAY_NAME, getValue());
	}

	@Override
	protected ImmutableValue<Text> getValueGetter() {
		return immutableValue;
	}

	@Override
	public OriginalDisplayNameData asMutable() {
		return new OriginalDisplayNameData(getValue());
	}

	@Override
	public int getContentVersion() {
		return OriginalDisplayNameData.CONTENT_VERSION;
	}
}
