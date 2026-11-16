package dev.qixils.crowdcontrol.common.command.impl.executeorperish;

import net.kyori.adventure.text.Component;

import static dev.qixils.crowdcontrol.common.util.RandomUtil.RNG;

public enum MathConditionGenerator {
	ADD {
		@Override
		public MathConditionState generate() {
			int operand1 = RNG.nextInt(100, 501);
			int operand2 = RNG.nextInt(100, 501);
			return new MathConditionState(operand1 + operand2, Component.text(operand1 + " + " + operand2), false);
		}
	},
	SUBTRACT {
		@Override
		public MathConditionState generate() {
			int operand1 = RNG.nextInt(100, 501);
			int operand2 = RNG.nextInt(100, 501);
			return new MathConditionState(operand1 - operand2, Component.text(operand1 + " - " + operand2), false);
		}
	},
	MULTIPLY {
		@Override
		public MathConditionState generate() {
			int operand1 = RNG.nextInt(15, 50);
			int operand2 = RNG.nextInt(3, 10);
			return new MathConditionState(operand1 * operand2, Component.text(operand1 + " * " + operand2), false);
		}
	},
	DIVIDE {
		@Override
		public MathConditionState generate() {
			int goal = RNG.nextInt(50, 200);
			int operand1 = goal * 2;
			int operand2 = 2;
			return new MathConditionState(goal, Component.text(operand1 + " / " + operand2), false);
		}
	},
	;

	public abstract MathConditionState generate();
}
