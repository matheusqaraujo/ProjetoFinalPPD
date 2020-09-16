package br.com.ppd.tuplespace.commands;

import br.com.ppd.tuplespace.models.Environment;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.service.JavaSpaceService;
import br.com.ppd.tuplespace.service.ServiceUnavailable;

import java.util.LinkedList;
import java.util.List;

import static br.com.ppd.tuplespace.util.Util.println;
import static java.lang.Float.parseFloat;

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
                moveUserXY();
                break;
            default:
                println("Target not supported.");
        }
    }

    private void moveUser() throws InvalidCommand {
        if (args.length != 6) throw new InvalidCommand("Correct usage: mv user <username> <new_env> <latitude> <longitude>");
        try {
            User user = (User) this.service.take(new User(args[2]));
            if (user != null) {
                Environment env = (Environment) this.service.read(new Environment(args[3], parseFloat(args[4]), parseFloat(args[5])));
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

    private void moveUserXY() throws InvalidCommand{
        if (args.length != 5) throw new InvalidCommand("Correct usage: mv user <username> <latitude> <longitude>");
        try {
            // Procura o user selecionado
            // Faz um take
            User user = (User) this.service.take(new User(args[2]));
            // Se encontra o user
            if (user != null) {
                // A partir da nova posicao calcula se tem alguem a 10m ou menos
                List<User> listUser = this.service.listAllUsers();
                List<User> lista = new LinkedList<User>();
                for (User item: listUser) {
                    double distance = Math.sqrt(Math.pow(item.latitude - parseFloat(args[3]), 2) + Math.pow(item.longitude - parseFloat(args[4]), 2));
                    println(String.format("Distancia entre o dispositivo %s -> %s = %.2fm", args[2], item.name, distance));
                    if (distance <= 10){
                        lista.add(item);
                    }
                }

                // Se tiver alguem perto cria um novo User com o ambiente do primeiro encontrado
                // Se n tiver ninguem perto, cria novo ambiente
                if(!lista.isEmpty()){ // Se tiver alguem a 10m ou menos:
                    // Encontra o ambiente do dispositivo mais proximo
                    Environment env = lista.get(0).environment;

                    // Cria dispositivo com o ambiente encontrado
                    //User user = new User();
                    //user.name = args[2];
                    user.latitude = parseFloat(args[3]);
                    user.longitude = parseFloat(args[4]);
                    if (this.service.searchUser(user) == null) {
                        user.environment = env;
                        //this.service.send(new User(args[2], env, parseFloat(args[3]), parseFloat(args[4])));
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

                    //User user = new User();
                    //user.name = args[2];
                    user.latitude = parseFloat(args[3]);
                    user.longitude = parseFloat(args[4]);
                    if (this.service.searchUser(user) == null) {
                        user.environment = env;
                        //this.service.send(new User(args[2], env, parseFloat(args[3]), parseFloat(args[4])));
                        println(String.format("Usuário %s adicionado a sala %s!", args[2], envName));
                    } else {
                        println(String.format("Usuário %s já está em outra sala!", args[2]));
                    }
                }


            } else {
                println(String.format("Could not find user %s", args[2]));
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
