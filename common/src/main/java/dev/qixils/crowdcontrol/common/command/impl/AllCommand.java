package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.VoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AllCommand<P> implements VoidCommand<P> {
	private final String effectName = null;
	private final Plugin<P, ?> plugin;

	@Override
	public void voidExecute(@NotNull List<@NotNull P> players, @NotNull Request request) {
		// Stop All Effects
		if (request.getType() == Request.Type.STOP) {
			if (plugin.isGlobal(request)) {
				TimedEffect.stopAll(null);
			} else {
				for (Request.Target target : request.getTargets()) {
					TimedEffect.stopAll(target);
				}
			}
		}
	}
}
