package io.github.lexikiq.crowdcontrol;

import lombok.Getter;

public enum ClassCooldowns {
    ENTITY(60),
    POTION(45)
    ;

    private final @Getter int seconds;
    ClassCooldowns(int seconds) {
        this.seconds = seconds;
    }
}
