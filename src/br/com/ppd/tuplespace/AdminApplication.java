package br.com.ppd.tuplespace;

import br.com.ppd.tuplespace.commands.AdminCommandProcessor;
import br.com.ppd.tuplespace.commands.InvalidCommand;

import java.util.Scanner;

import static br.com.ppd.tuplespace.util.Util.*;

class AdminApplication {

    private Scanner scanner;

    AdminApplication(){
        this.scanner = new Scanner(System.in);
    }

    void start() {
        boolean loop = true;
        showCommandsTable();
        String command;
        do {
            print(">> ");
            command = this.scanner.nextLine();
            try {
                AdminCommandProcessor.process(command);
            } catch (InvalidCommand|IllegalArgumentException invalidCommand) {
                println(invalidCommand.getMessage());
            }
        } while(loop);
    }

    private void showCommandsTable() {
        println("+-------------------------------------------+");
        println("+              Espaço de Tuplas             +");
        println("+-------------------------------------------+");
        println("Comandos adicionar: ");
        println("   add env <nome da sala>");
        println("   add user <nome do usuario> <nome da sala>");
        println("Comandos listar: ");
        println("   ls env");
        println("   ls user <nome da sala>");
        println("Comandos remover: ");
        println("   rm env <nome da sala>");
        println("   rm user <nome do usuario>");
        println("Comandos mover: ");
        println("   mv user <nome do usuario> <nome da sala>");
        println("+-------------------------------------------+");
    }

}
