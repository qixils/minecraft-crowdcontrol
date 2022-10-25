package io.github.lexikiq.crowdcontrol;

import lombok.Getter;

public enum ClassCooldowns {
    ENTITY(1),
    POTION(1),
    BLOCK(1),
    FALLING_BLOCK(1),
    WEATHER(1),
    TORCH(1),
    ENCHANTMENT(1)
    ;

    private final @Getter int seconds;
    ClassCooldowns(int seconds) {
        this.seconds = seconds;
    }
}
