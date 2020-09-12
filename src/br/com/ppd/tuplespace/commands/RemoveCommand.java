package br.com.ppd.tuplespace.commands;

import br.com.ppd.tuplespace.models.Environment;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.service.JavaSpaceService;
import br.com.ppd.tuplespace.service.ServiceUnavailable;

import static br.com.ppd.tuplespace.util.Util.println;

public class RemoveCommand implements ICommand {
    private JavaSpaceService service;
    private String[] args;

    public RemoveCommand() {
        this.service = JavaSpaceService.getInstance();
    }

    @Override
    public void execute(String[] args) throws InvalidCommand {
        ETarget target = null;
        try {
            target = ETarget.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidCommand("Unknown target.");
        }
        this.args = args;
        switch(target) {
            case ENV:
                removeEnv();
                break;
            case USER:
                removeUser();
                break;
        }
    }

    private void removeEnv() throws InvalidCommand {
        if (args.length != 3) throw new InvalidCommand("Correct usage: rm env <environment_name>");
        try {
            if (this.service.listUsersByEnv(args[2]).isEmpty()) {
                this.service.take(new Environment(args[2]));
                println("Environment deleted.");
            } else {
                println("Could not remove. Environment is not empty.");
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }

    private void removeUser() throws InvalidCommand {
        if (args.length != 3) throw new InvalidCommand("Correct usage: list env <environment_name>");
        try {
            User template = new User();
            template.name = args[2];
            User user = (User) this.service.take(template);
            if (user != null) {
                println(String.format("User %s removed from environment.", args[2]));
            } else {
                println(String.format("User %s not found.", args[2]));
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }
}
