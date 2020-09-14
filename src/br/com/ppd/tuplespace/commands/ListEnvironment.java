package br.com.ppd.tuplespace.commands;

import br.com.ppd.tuplespace.models.Environment;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.service.JavaSpaceService;
import br.com.ppd.tuplespace.service.ServiceUnavailable;

import java.util.List;

import static br.com.ppd.tuplespace.util.Util.println;

public class ListEnvironment implements ICommand {
    private JavaSpaceService service;
    private String[] args;

    public ListEnvironment() {
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
                listEnv();
                break;
            case USER:
                listUserInEnv();
                break;
            case USERS:
                listAllUsers();
                break;
        }
    }

    private void listEnv() throws InvalidCommand {
        if (args.length != 2) throw new InvalidCommand("Correct usage: list env");
        try {
            List<Environment> listEnv = this.service.listEnvironments();
            if (!listEnv.isEmpty()) {
                println("+ Salas");
                for(Environment env : listEnv) {
                    println("   - " + env.name + " (Latitude: " + env.latitude + " Longitude: " + env.longitude + ")");
                }
            } else {
                println("Salas não encontradas!");
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }

    private void listUserInEnv() throws InvalidCommand {
        if (args.length != 3) throw new InvalidCommand("Correct usage: list user <nome da sala>");
        try {
            List<User> listUser = this.service.listUsersByEnv(args[2]);
            if (!listUser.isEmpty()) {
                println("+ Sala: " + args[2]);
                println("+ Usuários");
                for(User env : listUser) {
                    println("   - " + env.name + " (Latitude: " + env.latitude + " Longitude: " + env.longitude + ")");
                }
            } else {
                println("Não foram encontrados usuários na sala " + args[2] + " !");
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }

    private void listAllUsers() throws InvalidCommand {
        if (args.length != 2) throw new InvalidCommand("Correct usage: list users");
        try {
            List<User> listUser = this.service.listAllUsers();
            if (!listUser.isEmpty()) {
                println("+ Usuários");
                for(User user : listUser) {
                    println("   - " + user.name + " (Ambiente: " + user.environment.name + " Latitude: " + user.latitude + " Longitude: " + user.longitude + ")");
                }
            } else {
                println("Usuários não encontradas!");
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }
}
