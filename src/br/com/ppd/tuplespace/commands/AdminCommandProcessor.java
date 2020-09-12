package br.com.ppd.tuplespace.commands;

public class AdminCommandProcessor {

    public static void process(String commandLine) throws InvalidCommand{
        String[] args = commandLine.split(" ");
        ECommand command;
        if (args.length < 2)
            throw new IllegalArgumentException("Command too short. Possibly missing arguments.");
        try {
            command = ECommand.valueOf(args[0].toUpperCase());
        } catch(IllegalArgumentException ex) {
            throw new InvalidCommand("Command not found.");
        }
        command.execute(args);
    }
}
