package dev.qixils.crowdcontrol.common.command.impl.executeorperish;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MathConditionState {
	private int goal = 0;
	private Component component = null;
	private boolean succeeded = false;
}
