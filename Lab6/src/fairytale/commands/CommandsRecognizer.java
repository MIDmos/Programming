package fairytale.commands;

import java.io.PrintStream;
import java.util.LinkedList;

public class CommandsRecognizer {//Класс для распознавания команд
    private LinkedList<Command>commands;
    private PrintStream printStream;

    public CommandsRecognizer(LinkedList<Command> commands){
        this.commands=commands;
        printStream=System.out;
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
                }else printStream.println("Количество аргументов не совпало с ожидаемым");
            }
        }
        if(!isCommand)printStream.println("Нет такой команды");
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    public LinkedList<Command> getCommands() {
        return commands;
    }

    public void setCommands(LinkedList<Command> commands) {
        this.commands = commands;
    }
}
