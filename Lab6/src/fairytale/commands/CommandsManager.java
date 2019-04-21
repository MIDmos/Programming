package fairytale.commands;

import java.io.PrintStream;
import java.util.LinkedList;
/**
 * Обрабатывает команды, Обладает двумя встроенными:
 * help - выводит список всех команды
 * exit - завершает работу по распознаванию команд
 */
public class CommandsManager {//Обработчик команд
    private LinkedList<Command>commands;
    private CommandsRecognizer recognizer;
    private boolean active;
    private PrintStream printStream;

    public CommandsManager(){
        printStream=System.out;
        commands=new LinkedList<>();
        addCommand(
                new Command("help",0) {
                    @Override
                    public void execute() {
                        println("Все команды:");
                        for (Command command:commands) {
                            print(command);
                            print("           ");
                            command.describe();
                            println();
                        }
                    }

                    @Override
                    public void describe() {
                        println("Выводит список всех команд с описанием.");
                    }
                },
                new Command("NULL",-2) {
                    @Override
                    public void execute() {
                        println("Ничего не произошло");
                    }
                    @Override
                    public void describe() {
                        println("Ничего не делает.");
                    }
                },
                new Command("exit",0) {
                    @Override
                    public void execute() {
                        println("До свидания.");
                        active=false;
                    }
                    @Override
                    public void describe() {
                        println("Завершает работу");
                    }
                });
        recognizer=new CommandsRecognizer(commands);
        active=true;
    }

    public void addCommand(Command... commandList){
        for(Command command:commandList)
            commands.add(command);
    }

    public void print(Object o){
        printStream.print(o);
    }

    public void println(Object o){
        printStream.println(o);
    }

    public void println(){
        printStream.println();
    }


    public void doCommand(String command){
        recognizer.recognizeCommand(command);
    }

    public void doCommand(CommandDescriptor descriptor){
        recognizer.recognizeCommand(descriptor);
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
        recognizer.setPrintStream(printStream);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public CommandsRecognizer getRecognizer() {
        return recognizer;
    }

    public void setRecognizer(CommandsRecognizer recognizer) {
        this.recognizer = recognizer;
    }

    public LinkedList<Command> getCommands() {
        return commands;
    }

    public void setCommands(LinkedList<Command> commands) {
        this.commands = commands;
    }
}
