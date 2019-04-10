package fairytale.commands;

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

    public CommandsManager(){
        commands=new LinkedList<>();
        addCommand(
                new Command("help",0) {
                    @Override
                    public void execute() {
                        System.out.println("Все команды:");
                        for (Command command:commands) {
                            System.out.print(command);
                            System.out.print("   ");
                            command.describe();
                            System.out.println();
                        }
                    }

                    @Override
                    public void describe() {
                        System.out.println("Выводит список всех команд с описанием.");
                    }
                },
                new Command("NULL",-2) {
                    @Override
                    public void execute() {
                        System.out.println("Ничего не произошло");
                    }
                    @Override
                    public void describe() {
                        System.out.println("Ничего не делает.");
                    }
                },
                new Command("exit",0) {
                    @Override
                    public void execute() {
                        System.out.println("До свидания.");
                        active=false;
                    }
                    @Override
                    public void describe() {
                        System.out.println("Завершает работу");
                    }
                });
        recognizer=new CommandsRecognizer(commands);
        active=true;
    }

    public void addCommand(Command... commandList){
        for(Command command:commandList)
            commands.add(command);
    }

    public void doCommand(String command){
        recognizer.recognizeCommand(command);
    }

    public void doCommand(CommandDescriptor descriptor){
        recognizer.recognizeCommand(descriptor);
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
