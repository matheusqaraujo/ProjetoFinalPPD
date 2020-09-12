package br.com.ppd.tuplespace.commands;

public class InvalidCommand extends Exception {

    public InvalidCommand(String msg) {
        super(msg);
    }
}
