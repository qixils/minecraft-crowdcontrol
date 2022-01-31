package dev.qixils.crowdcontrol.plugin.sponge8.data.entity;

import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class OriginalDisplayNameDataBuilder extends AbstractDataBuilder<OriginalDisplayNameData>
		implements DataManipulatorBuilder<OriginalDisplayNameData, ImmutableOriginalDisplayNameData> {

	public OriginalDisplayNameDataBuilder() {
		super(OriginalDisplayNameData.class, OriginalDisplayNameData.CONTENT_VERSION);
	}

	@Override
	public OriginalDisplayNameData create() {
		return new OriginalDisplayNameData();
	}

	@Override
	public Optional<OriginalDisplayNameData> createFrom(DataHolder dataHolder) {
		return create().fill(dataHolder);
	}

	@Override
	protected Optional<OriginalDisplayNameData> buildContent(DataView container) throws InvalidDataException {
		if (container.contains(SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME)) {
			//noinspection OptionalGetWithoutIsPresent -- check is implied by the if statement
			return Optional.of(new OriginalDisplayNameData(container.getObject(SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME.getQuery(), Text.class).get()));
		}
		return Optional.empty();
	}

}
