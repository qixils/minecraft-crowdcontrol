package io.github.lexikiq.crowdcontrol;

import lombok.Getter;

@Getter
public class CommandWrapper {
    private final ChatCommand command;
    private final String[] args;
    public CommandWrapper(ChatCommand command, String... args) {
        this.command = command;
        this.args = args;
    }
}
