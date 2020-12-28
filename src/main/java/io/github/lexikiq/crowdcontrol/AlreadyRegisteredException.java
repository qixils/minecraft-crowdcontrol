package io.github.lexikiq.crowdcontrol;

public class AlreadyRegisteredException extends Exception {
    private final String commandName;
    public AlreadyRegisteredException(String commandName){
        this.commandName = commandName;
    }
    // idk why lombok was refusing to work here
    public String getCommandName() {
        return commandName;
    }
}
