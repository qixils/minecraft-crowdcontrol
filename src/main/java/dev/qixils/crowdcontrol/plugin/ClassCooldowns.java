package dev.qixils.crowdcontrol.plugin;

import lombok.Getter;

public enum ClassCooldowns {
    ENTITY(20),
    POTION(20),
    BLOCK(5),
    FALLING_BLOCK(20),
    WEATHER(20),
    TORCH(20),
    ENCHANTMENT(20)
    ;

    private final @Getter int seconds;
    ClassCooldowns(int seconds) {
        this.seconds = seconds;
    }
}
