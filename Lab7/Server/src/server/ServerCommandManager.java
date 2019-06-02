package server;

import fairytale.FileHandler;
import fairytale.Noise;
import fairytale.commands.Command;
import fairytale.commands.CommandsManager;
import fairytale.commands.CommandsRecognizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class ServerCommandManager extends CommandsManager {

    private Server server;
    private Connection dbConnecion;
    private String token;
    private long id;
    private Set<Noise>noises;

    public ServerCommandManager(Connection connection, Server server, String token, long id, Set<Noise>noises) {
        this.server=server;
        this.token=token;
        this.id=id;
        dbConnecion=connection;
        setPrintStream(System.out);
        setCommands(new LinkedList<>());
        this.noises=noises;
        addCommand(
                new Command("help",0) {
                    @Override
                    public void execute() {
                        println("Все команды:");
                        getCommands().forEach(c-> {
                            print(c);
                            print("           ");
                            c.describe();
                            println();
                        });
                    }

                    @Override
                    public void describe() {
                        println("Выводит список всех команд с описанием.");
                    }
                },
                new Command("exit",0) {
                    @Override
                    public void execute() {
                        server.removeUser(token);
                        println("До свидания.");
                        setActive(false);
                    }
                    @Override
                    public void describe() {
                        println("Завершает работу");
                    }
                },
                new Command("add", -1) {
                    @Override
                    public void execute() {
                        if (loginConfirmed()) {
                            Noise.fromJson(getId(),getArguments()).forEach(noise -> {
                                if(noises.add(noise)) {
                                    addNoise(noise);
                                    println(noise + " добавлен");
                                }else
                                    println(noise+" не добавлен");
                            });
                        }
                    }
                    @Override
                    public void describe() {
                        println("Добавляет в коллекцию элемент типа Noise");
                    }
                },
                new Command("remove_greater", -1) {
                    @Override
                    public void execute(){
                        if (loginConfirmed()) {
                            LinkedHashSet<Noise>args=Noise.fromJson(getId(),getArguments());
                            noises.removeIf(noise -> {
                                if (noise.getOwnerId()==getId()
                                        && noise.compareTo(
                                        args.stream()
                                                .max(Noise::compareTo)
                                                .get()) > 0) {
                                    removeNoise(noise);
                                    println(noise + " удален");
                                    return true;
                                }
                                if (noise.getOwnerId()!=getId()){
                                    println("У вас недостаточно прав для удаления "+noise);
                                }
                                println(noise + " оставлен в коллекции");
                                return false;
                            });
                        }
                    }
                    @Override
                    public void describe() {
                        println("Удаляет все элементы коллекции, превышающие заданный.");
                    }
                },
                new Command("show", 0) {
                    @Override
                    public void execute() {
                        noises.forEach(ServerCommandManager.this::println);
                        if (noises.size() == 0)
                            println("Коллекция пуста");
                    }
                    @Override
                    public void describe() {
                        println("Выводит список всех элементов коллекции.");
                    }
                },
                new Command("save", 0) {
                    @Override
                    public void execute() {
                        if (loginConfirmed()) {
                            FileHandler.writeFile(noises);
                            println("Коллекция сохранена");
                        }
                    }
                    @Override
                    public void describe() {
                        println("Сохраняет коллекцию в файл save.json.");
                    }
                },
                new Command("clear", 0) {
                    @Override
                    public void execute() {
                        if (loginConfirmed()) {
                            noises.removeIf(noise -> {
                                if(noise.getOwnerId()==getId()){
                                    removeNoise(noise);
                                    println(noise+" удален");
                                    return true;
                                }
                                println("У вас недостаточно прав для удаления ");
                                println(noise+" oставлен в коллекции");
                                return false;
                            });
                        }
                    }
                    @Override
                    public void describe() {
                        println("Удаляет все элементы коллекции.");
                    }
                },
                new Command("info", 0) {
                    @Override
                    public void execute() {
                        println("\nКоллекция шумов");
                        println("Тип коллекции: " + noises.getClass().getName());
                        println("Тип элементов: " + Noise.class.getName());
                        println("Всего элементов: " + noises.size());
                        println("Дата инициализации: " + new Date(server.INIT_TIME) + '\n');
                    }
                    @Override
                    public void describe() {
                        println("Выводит информацию о коллекции.");
                    }
                },
                new Command("remove_lower", -1) {
                    @Override
                    public void execute() {
                        if (loginConfirmed()) {
                            LinkedHashSet<Noise> args = Noise.fromJson(getId(),getArguments());
                            if (args.size() > 0) {
                                if (noises.removeIf(setNoise -> {
                                    if (setNoise.getOwnerId()==getId()
                                            && setNoise.compareTo(
                                            args.stream()
                                                    .max(Noise::compareTo)
                                                    .get()) < 0) {
                                        removeNoise(setNoise);
                                        println(setNoise + "удален");
                                        return true;
                                    }
                                    if (setNoise.getOwnerId()!=getId()){
                                        println("У вас недостаточно прав для удаления "+setNoise);
                                    }
                                    println(setNoise + "оставлен в коллекции");
                                    return false;
                                })) ;
                            }
                        }
                    }
                    @Override
                    public void describe() {
                        println("Удаляет все элементы коллекции, меньшие заданного.");
                    }
                },
                new Command("remove", -1) {
                    @Override
                    public void execute() {
                        if (loginConfirmed()) {

                            LinkedHashSet<Noise> args = Noise.fromJson(getId(),getArguments());
                            if (args.size() > 0) {
                                if (noises.removeIf(setNoise -> {
                                    if (setNoise.getOwnerId()==getId()
                                            && args.stream()
                                            .anyMatch(noise -> noise.compareTo(setNoise)==0)) {
                                        removeNoise(setNoise);
                                        println(setNoise + "удален");
                                        return true;
                                    }
                                    if (setNoise.getOwnerId()!=getId()){
                                        println("У вас недостаточно прав для удаления "+setNoise);
                                    }
                                    println(setNoise + "оставлен в коллекции");
                                    return false;
                                })) ;
                            }
                        }
                    }
                    @Override
                    public void describe() {
                        println("Удаляет заданный элементы коллекции.");
                    }
                },
                new Command("load", 1) {
                    @Override
                    public void execute() {
                        if (loginConfirmed()) {
                            FileHandler.readFile(getArguments()).forEach(noise -> {
                                if(noises.add(noise)){
                                    addNoise(noise);
                                    println(noise + " добавлен");
                                }else {
                                    println(noise + " не добавлен");
                                }
                            });
                        }
                    }
                    @Override
                    public void describe() {
                        println("Дополняет коллекцию элементами из файла с сервера.");
                    }
                },
                new Command("add_if_max", -1) {
                    @Override
                    public void execute() {
                        if (loginConfirmed()) {
                            if (noises.size() > 0) {
                                Noise max = noises.stream().max(Noise::compareTo).get();
                                noises.addAll(
                                        Noise.fromJson(getId(),getArguments()).stream()
                                                .filter(noise -> {
                                                    if (noise.compareTo(max) > 0) {
                                                        addNoise(noise);
                                                        println(noise + " добавлен");
                                                        return true;
                                                    }
                                                    println(noise + "не добавлен");
                                                    return false;
                                                })
                                                .collect(Collectors.toSet()));
                            } else noises.addAll(Noise.fromJson(getId(),getArguments()));
                        }
                    }
                    @Override
                    public void describe() {
                        println("Добавляет элемент в коллекцию, если он больше максимального.");
                    }
                });
        setRecognizer(new CommandsRecognizer(getCommands()));
        setActive(true);
    }
    public void addNoise(Noise noise){
        try {
            PreparedStatement statement = dbConnecion.prepareStatement(QueryStorage.ADD_NOISE);
            statement.setLong(1,id);
            statement.setString(2,noise.getNAME());
            statement.setString(3,noise.getSound());
            statement.setInt(4,noise.getDISTANCE());
            statement.setTimestamp(5, Timestamp.valueOf(noise.getCREATION_TIME()));

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void removeNoise(Noise noise){
        try {
            PreparedStatement statement = dbConnecion.prepareStatement(QueryStorage.DELETE_NOISE_BY_NAME);
            statement.setLong(1,id);
            statement.setString(2,noise.getNAME());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean loginConfirmed() {
        if(server.userLogined(token))
            return true;
        println("Ваша авторизация не подтверждена");
        return false;
    }

    public Set<Noise> getNoises() {
        return noises;
    }
    public void setNoises(Set<Noise> noises) {
        this.noises = noises;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
}
