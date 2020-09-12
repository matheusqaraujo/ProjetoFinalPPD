package br.com.ppd.tuplespace.commands;

import br.com.ppd.tuplespace.models.Environment;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.service.JavaSpaceService;
import br.com.ppd.tuplespace.service.ServiceUnavailable;

import static br.com.ppd.tuplespace.util.Util.println;

public class MoveCommand implements ICommand {
    private JavaSpaceService service;
    private String[] args;

    public MoveCommand() {
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
            case USER:
                moveUser();
                break;
            default:
                println("Target not supported.");
        }
    }

    private void moveUser() throws InvalidCommand {
        if (args.length != 4) throw new InvalidCommand("Correct usage: mv user <username> <new_env>");
        try {
            User user = (User) this.service.take(new User(args[2]));
            if (user != null) {
                Environment env = (Environment) this.service.read(new Environment(args[3]));
                if (env != null) {
                    user.environment = env;
                    this.service.send(user);
                    println(String.format("User %s moved to environment %s", args[2], args[3]));
                } else {
                    println(String.format("Could not find environment %s", args[3]));
                }
            } else {
                println(String.format("Could not find user %s", args[2]));
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }
}
