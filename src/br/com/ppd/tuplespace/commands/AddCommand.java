package br.com.ppd.tuplespace.commands;

import br.com.ppd.tuplespace.models.Environment;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.service.JavaSpaceService;
import br.com.ppd.tuplespace.service.ServiceUnavailable;

import static br.com.ppd.tuplespace.util.Util.println;
import static java.lang.Float.parseFloat;

public class AddCommand implements ICommand {

    private JavaSpaceService service;
    private String[] args;

    public AddCommand() {
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
                addEnv();
                break;
            case USER:
                addUser();
                break;
        }
    }

    private void addEnv() throws InvalidCommand {
        if (args.length != 5) throw new InvalidCommand("Correct usage: add env <nome da sala> <latitude> <longitude>");
        String envName = args[2];
        Float envLatitude = parseFloat(args[3]);
        Float envLongitude = parseFloat(args[4]);
        try {
            this.service.send(new Environment(envName, envLatitude, envLongitude));
            println(String.format("Sala %s adicionada", envName));
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }

    private void addUser() throws InvalidCommand {
        if (args.length != 6) throw new InvalidCommand("Correct usage: add user <nome do usuario> <nome da sala> <latitude> <longitude>");
        try {
            Environment env = this.service.findEnvironment(args[3], parseFloat(args[4]), parseFloat(args[5]));
            if (env == null) throw new IllegalArgumentException(String.format("Sala %s não encontrada.", args[3]));

            User user = new User();
            user.name = args[2];
            user.latitude = parseFloat(args[4]);
            user.longitude = parseFloat(args[5]);
            if (this.service.searchUser(user) == null) {
                user.environment = env;
                this.service.send(new User(args[2], env, parseFloat(args[4]), parseFloat(args[5])));
                println(String.format("Usuário %s adicionado a sala %s!", args[2], args[3]));
            } else {
                println(String.format("Uusário %s já está em outra sala!", args[2]));
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }
}
