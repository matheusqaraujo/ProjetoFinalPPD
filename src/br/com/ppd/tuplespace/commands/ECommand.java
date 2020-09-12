package br.com.ppd.tuplespace.commands;

public enum ECommand {
    ADD(new AddCommand()),
    LS(new ListEnvironment()),
    RM(new RemoveCommand()),
    MV(new MoveCommand());

    private ICommand command;

    ECommand(ICommand command) {
        this.command = command;
    }

    public void execute(String[] args) throws InvalidCommand { this.command.execute(args);}
}
