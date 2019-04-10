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
    private LinkedHashSet<Noise> noises;
    private Karlson karlson;
    private Kid kid;
    private CommandsManager commandsManager;
    private long initTime;

    public Story() {
        //Создание главных персонажей
        karlson = new Karlson("Карлсон", 2, 100);
        kid = new Kid("Малыш", 3, 50);
        kid.setConcentration(0);

        noises = new LinkedHashSet<>();//Создание шумов
        initTime = System.currentTimeMillis();

        commandsManager = new CommandsManager();
        commandsManager.addCommand(//Добавление необходимых команд
                new Command("add", -1) {
                    @Override
                    public void execute() {
                        if(noises.addAll(createNoise(getArguments())))
                            System.out.println("Успешное добавление");
                        else System.out.println("При добавлении возникли проблемы");
                    }
                    @Override
                    public void describe() {
                        System.out.println("Добавляет в коллекцию элемент типа Noise");
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
                                System.out.println(noise+" удален");
                                return true;
                            }
                            System.out.println(noise + " оставлен в коллекции");
                            return false;
                        });
                    }
                    @Override
                    public void describe() {
                        System.out.println("Удаляет все элементы коллекции, превышающие заданный.");
                    }
                },
                new Command("show", 0) {
                    @Override
                    public void execute() {
                        for (Noise noise : noises) System.out.println(noise);
                        if(noises.size()==0)
                            System.out.println("Коллекция пуста");
                    }
                    @Override
                    public void describe() {
                        System.out.println("Выводит список всех элементов коллекции.");
                    }
                },
                new Command("save", 0) {
                    @Override
                    public void execute() {
                        FileHandler.writeFile(noises);
                    }
                    @Override
                    public void describe() {
                        System.out.println("Сохраняет коллекцию в файл save.json.");
                    }
                },
                new Command("clear", 0) {
                    @Override
                    public void execute() {
                        noises.clear();
                    }
                    @Override
                    public void describe() {
                        System.out.println("Удаляет все элементы коллекции.");
                    }
                },
                new Command("info", 0) {
                    @Override
                    public void execute() {
                        System.out.println("\nКоллекция шумов");
                        System.out.println("Тип коллекции: " + noises.getClass().getName());
                        System.out.println("Тип элементов: " + Noise.class.getName());
                        System.out.println("Всего элементов: " + noises.size());
                        System.out.println("Дата инициализации: " + new Date(initTime) + '\n');
                    }
                    @Override
                    public void describe() {
                        System.out.println("Выводит информацию о коллекции.");
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
                                    System.out.println(setNoise + "удален");
                                    return true;
                                }
                                System.out.println(setNoise + "оставлен в коллекции");
                                return false;
                            }));
                        }
                    }
                    @Override
                    public void describe() {
                        System.out.println("Удаляет все элементы коллекции, меньшие заданного.");
                    }
                },
                new Command("remove", -1) {
                    @Override
                    public void execute() {
                        if(noises.removeAll(createNoise(getArguments())))
                            System.out.println("Операция прошла успешно");
                        else System.out.println("Ошибка при удалении");
                    }
                    @Override
                    public void describe() {
                        System.out.println("Удаляет заданный элементы коллекции.");
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
                                            System.out.println(noise+" добавлен");
                                            return true;
                                        }
                                        System.out.println(noise + "не добавлен");
                                        return false;
                                    })
                                    .collect(Collectors.toSet()));
                        }else noises.addAll(createNoise((getArguments())));
                    }
                    @Override
                    public void describe() {
                        System.out.println("Добавляет элемент в коллекцию, если он больше максимального.");
                    }
                },
                new Command("play", 0) {
                    @Override
                    public void execute() {
                        kid.listen(karlson.createSound(), noises);
                        karlson.playSound();
                        System.out.println();


                        System.out.println("Малыш слушал рассеянно.");
                        for (Noise noise : noises)
                            if (noise.wasActive()) {
                                System.out.println("Подействовал шум " + noise);
                                noise.setWasActive(false);
                            }
                        System.out.println();

                        kid.playSound();
                    }
                    @Override
                    public void describe() {
                        System.out.println("Позволяет сыграть историю.");
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

    private Noise findMax(LinkedHashSet<Noise> set){
        return new TreeSet<>(set).last();
    }

    public void setNoises(LinkedHashSet<Noise> noises) {
        this.noises = noises;
        initTime = System.currentTimeMillis();
    }

    public LinkedHashSet<Noise> getNoises() {
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