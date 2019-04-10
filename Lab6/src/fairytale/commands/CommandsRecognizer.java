package fairytale.commands;

import java.util.LinkedList;

public class CommandsRecognizer {//Класс для распознавания команд
    private LinkedList<Command>commands;

    public CommandsRecognizer(LinkedList<Command> commands){
        this.commands=commands;
    }

    public void recognizeCommand(String commandIn){
        CommandDescriptor descriptor=new CommandDescriptor(commandIn);
        if(descriptor.getARGS_COUNT()!=-2)
            recognizeCommand(descriptor);
    }

    public void recognizeCommand(CommandDescriptor descriptor){
        boolean isCommand=false;

        for(Command command:commands){
            if(command.getNAME().equals(descriptor.getNAME())){
                isCommand=true;
                if(command.getARGS_COUNT()==descriptor.getARGS_COUNT()||(command.getARGS_COUNT()<0)){
                    command.setArguments(descriptor.getArguments());
                    command.execute();
                }else System.out.println("Количество аргументов не совпало с ожидаемым");
            }
        }
        if(!isCommand)System.out.println("Нет такой команды");
    }

    public LinkedList<Command> getCommands() {
        return commands;
    }

    public void setCommands(LinkedList<Command> commands) {
        this.commands = commands;
    }
}
