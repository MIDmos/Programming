package fairytale;

import fairytale.commands.CommandsManager;
import java.util.Scanner;

//Класс, для запуска программы
public class Main {
    public static void main(String[] args){

        System.out.println("Добрый день!");
        Story story=new Story();
        CommandsManager commandsManager=story.getCommandsManager();

        Scanner scanner=new Scanner(System.in);

        String pathToFile=System.getenv("NOISES");
        if(pathToFile!=null)
            commandsManager.doCommand("import "+pathToFile);
        else
            System.out.println("Файл для загрузки не выбран.\nКоллекция пустая.");
        while (commandsManager.isActive()){
            System.out.print("Ваша команда: ");
            synchronized (story) {
                commandsManager.doCommand(scanner.nextLine());
            }
        }
        commandsManager.doCommand("save");
    }
}
