package dev.qixils.crowdcontrol.plugin;

public enum CommandType {
    CHAT(true),
    BITS(false),
    POINTS(false)
    ;

    private final boolean useCooldown;
    CommandType(boolean useCooldown) {
        this.useCooldown = useCooldown;
    }
    public boolean usesCooldown() {
        return useCooldown;
    }
}
