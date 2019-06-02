package fairytale.commands;

import fairytale.Describable;

public abstract class Command implements Describable {

    private final String NAME;
    private final int ARGS_COUNT;
    private String arguments;
    /**
     * @param name- имя команды
     * @param argsCount - количество аргументов. Если оно меньше нуля - бесконечно
     */
    public Command(String name,int argsCount){
        NAME=name;
        ARGS_COUNT=argsCount;
    }

    public abstract void execute();

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "Команда "+NAME;
    }

    public String getNAME() {
        return NAME;
    }

    public int getARGS_COUNT() {
        return ARGS_COUNT;
    }
}