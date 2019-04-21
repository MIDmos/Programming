package fairytale;

import fairytale.commands.Command;
import fairytale.commands.CommandsManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ivan
 * @version 1.0
 *
 * <p>Класс, реализующий историю
 * Работает с коллекцией шумов {@link Noise}
 * Добавляет в обработчик команд следующие команды {@link Command}:
 * <p>aremove {element}: удалить элемент из коллекции по его значению
 * <p>show: вывести в стандартный поток вывода все элементы коллекции в строковом представлении
 * <p>save: сохранить коллекцию в файл
 * <p>add_if_max {element}: добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции
 * <p>remove_greater {element}: удалить из коллекции все элементы, превышающие заданный
 * <p>remove_lower {element}: удалить из коллекции все элементы, меньшие, чем заданный
 * <p>info: вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
 * <p>clear: очистить коллекцию
 * <p>import {String path}: добавить в коллекцию все данные из файла
 * <p>add {element}: добавить новый элемент в коллекцию
 * <p>Формат задания элементов в командах - json
 * <p>Также обладает встроенными командами класса {@link CommandsManager}
 */
public class Story {//Класс, реализующий историю
    private Set<Noise> noises;
    private CommandsManager commandsManager;
    private long initTime;

    public Story() {
        initTime = System.currentTimeMillis();

        noises =Collections.synchronizedSet(new LinkedHashSet<>());//Создание шумов

        commandsManager = new CommandsManager();
        commandsManager.addCommand(//Добавление необходимых команд
                new Command("add", -1) {
                    @Override
                    public void execute() {
                        if(noises.addAll(createNoise(getArguments())))
                            commandsManager.println("Успешное добавление");
                        else commandsManager.println("При добавлении возникли проблемы");
                    }
                    @Override
                    public void describe() {
                        commandsManager.println("Добавляет в коллекцию элемент типа Noise");
                    }
                },
                new Command("remove_greater", -1) {
                    @Override
                    public void execute(){
                        LinkedHashSet<Noise> args = createNoise(getArguments());
                        noises.removeIf(noise -> {
                            if(noise.compareTo(
                                    args.stream()
                                    .max(Noise::compareTo)
                                    .get()) > 0){
                                commandsManager.println(noise+" удален");
                                return true;
                            }
                            commandsManager.println(noise + " оставлен в коллекции");
                            return false;
                        });
                    }
                    @Override
                    public void describe() {
                        commandsManager.println("Удаляет все элементы коллекции, превышающие заданный.");
                    }
                },
                new Command("show", 0) {
                    @Override
                    public void execute() {
                        noises.forEach(commandsManager::println);
                        if(noises.size()==0)
                            commandsManager.println("Коллекция пуста");
                    }
                    @Override
                    public void describe() {
                        commandsManager.println("Выводит список всех элементов коллекции.");
                    }
                },
                new Command("save", 0) {
                    @Override
                    public void execute() {
                        FileHandler.writeFile(noises);
                    }
                    @Override
                    public void describe() {
                        commandsManager.println("Сохраняет коллекцию в файл save.json.");
                    }
                },
                new Command("clear", 0) {
                    @Override
                    public void execute() {
                        noises.clear();
                    }
                    @Override
                    public void describe() {
                        commandsManager.println("Удаляет все элементы коллекции.");
                    }
                },
                new Command("info", 0) {
                    @Override
                    public void execute() {
                        commandsManager.println("\nКоллекция шумов");
                        commandsManager.println("Тип коллекции: " + noises.getClass().getName());
                        commandsManager.println("Тип элементов: " + Noise.class.getName());
                        commandsManager.println("Всего элементов: " + noises.size());
                        commandsManager.println("Дата инициализации: " + new Date(initTime) + '\n');
                    }
                    @Override
                    public void describe() {
                        commandsManager.println("Выводит информацию о коллекции.");
                    }
                },
                new Command("remove_lower", -1) {
                    @Override
                    public void execute() {
                        LinkedHashSet<Noise> args = createNoise(getArguments());
                        if (args.size() > 0) {
                            if(noises.removeIf(setNoise -> {
                                if(setNoise.compareTo(
                                        args.stream()
                                        .max(Noise::compareTo)
                                        .get()) < 0){
                                    commandsManager.println(setNoise + "удален");
                                    return true;
                                }
                                commandsManager.println(setNoise + "оставлен в коллекции");
                                return false;
                            }));
                        }
                    }
                    @Override
                    public void describe() {
                        commandsManager.println("Удаляет все элементы коллекции, меньшие заданного.");
                    }
                },
                new Command("remove", -1) {
                    @Override
                    public void execute() {
                        if(noises.removeAll(createNoise(getArguments())))
                            commandsManager.println("Операция прошла успешно");
                        else commandsManager.println("Ошибка при удалении");
                    }
                    @Override
                    public void describe() {
                        commandsManager.println("Удаляет заданный элементы коллекции.");
                    }
                },
                new Command("add_if_max", -1) {
                    @Override
                    public void execute() {
                        if (noises.size() > 0) {
                            Noise max =noises.stream().max(Noise::compareTo).get();
                            noises.addAll(
                            createNoise(getArguments()).stream()
                                    .filter(noise -> {
                                        if(noise.compareTo(max) > 0){
                                            commandsManager.println(noise+" добавлен");
                                            return true;
                                        }
                                        commandsManager.println(noise + "не добавлен");
                                        return false;
                                    })
                                    .collect(Collectors.toSet()));
                        }else noises.addAll(createNoise((getArguments())));
                    }
                    @Override
                    public void describe() {
                        commandsManager.println("Добавляет элемент в коллекцию, если он больше максимального.");
                    }
                }
        );
    }

    public CommandsManager getCommandsManager() {
        return commandsManager;
    }

    public void setCommandsManager(CommandsManager commandsManager) {
        this.commandsManager = commandsManager;
    }

    public void setNoises(Set<Noise> noises) {
        this.noises = Collections.synchronizedSet(noises);
        initTime = System.currentTimeMillis();
    }

    public Set<Noise> getNoises() {
        return noises;
    }

    public LinkedHashSet<Noise> createNoise(String arguments) {
        LinkedHashSet<Noise> set = new LinkedHashSet<>();
        try(Scanner scanner = new Scanner(arguments)) {
            JsonParser parser = new JsonParser(scanner, set);
            parser.parse();
        }
        return set;
    }
}