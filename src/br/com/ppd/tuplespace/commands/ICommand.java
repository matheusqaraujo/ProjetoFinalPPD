package br.com.ppd.tuplespace.commands;

public interface ICommand {

    void execute(String[] args) throws InvalidCommand;
}
