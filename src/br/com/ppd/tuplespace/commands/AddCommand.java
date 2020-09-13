package br.com.ppd.tuplespace.commands;

import br.com.ppd.tuplespace.models.Environment;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.service.JavaSpaceService;
import br.com.ppd.tuplespace.service.ServiceUnavailable;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
                addUser10m();
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
            Environment env = this.service.findEnvironment(args[3]);
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
                println(String.format("Usuário %s já está em outra sala!", args[2]));
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }
    
    private void addUser10m() throws InvalidCommand {
        if (args.length != 5) throw new InvalidCommand("Correct usage: add user <nome do usuario> <latitude> <longitude>");
        try {
            List<User> listUser = this.service.listAllUsers();
            List<User> lista = new LinkedList<User>();
            for (User item: listUser) {
                double distance = Math.sqrt(Math.pow(item.latitude - parseFloat(args[3]), 2) + Math.pow(item.longitude - parseFloat(args[4]), 2));
                println(String.format("Distancia entre o dispositivo %s -> %s = %f.2", args[2], item.name, distance));
                if (distance <= 10){
                    lista.add(item);
                }
            }

            if(!lista.isEmpty()){ // Se tiver alguem a 10m ou menos:
                // Encontra o ambiente do dispositivo mais proximo
                Environment env = lista.get(0).environment;

                // Cria dispositivo com o ambiente encontrado
                User user = new User();
                user.name = args[2];
                user.latitude = parseFloat(args[3]);
                user.longitude = parseFloat(args[4]);
                if (this.service.searchUser(user) == null) {
                    user.environment = env;
                    this.service.send(new User(args[2], env, parseFloat(args[3]), parseFloat(args[4])));
                    println(String.format("Usuário %s adicionado a sala %s!", args[2], env.name));
                } else {
                    println(String.format("Usuário %s já está em outra sala!", args[2]));
                }
            }
            else{ // Caso não tenha ninguem:
                // Cria ambiente
                String envName = getAlphaNumericString(2);
                Float envLatitude = parseFloat(args[3]);
                Float envLongitude = parseFloat(args[4]);
                try {
                    this.service.send(new Environment(envName, envLatitude, envLongitude));
                    println(String.format("Sala %s adicionada", envName));
                } catch (ServiceUnavailable serviceUnavailable) {
                    println("Could not execute command. Error: " + serviceUnavailable.getMessage());
                }

                // Cria dispositivo
                Environment env = this.service.findEnvironment(envName);
                if (env == null) throw new IllegalArgumentException(String.format("Sala %s não encontrada.", args[3]));

                User user = new User();
                user.name = args[2];
                user.latitude = parseFloat(args[3]);
                user.longitude = parseFloat(args[4]);
                if (this.service.searchUser(user) == null) {
                    user.environment = env;
                    this.service.send(new User(args[2], env, parseFloat(args[3]), parseFloat(args[4])));
                    println(String.format("Usuário %s adicionado a sala %s!", args[2], envName));
                } else {
                    println(String.format("Usuário %s já está em outra sala!", args[2]));
                }
            }

        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }

    static String getAlphaNumericString(int n)
    {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
