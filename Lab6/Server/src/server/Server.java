package server;

import fairytale.FileHandler;
import fairytale.Story;
import fairytale.commands.Command;
import fairytale.commands.CommandsManager;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Server {

    private DatagramChannel channel;
    private final Story story;
    private final CommandsManager manager;


    public Server(int port) throws IOException {
        channel = DatagramChannel.open().bind(new InetSocketAddress("localhost", port));
        System.out.println("Сервер запущен");
        System.out.println("IP: " + InetAddress.getLocalHost());
        System.out.println("Порт: " + port);

        story = new Story();

        manager = story.getCommandsManager();
        manager.addCommand(
                new Command("connect", 0) {
                    @Override
                    public void execute() {
                        System.out.println("подключено");
                    }

                    @Override
                    public void describe() {
                        System.out.println("Команда для проверки соединения");
                    }
                },
                new Command("load", 1) {
                    @Override
                    public void execute() {
                        if (story.getNoises().addAll(FileHandler.readFile(getArguments())))
                            System.out.println("Коллекция изменена");
                        else System.out.println("Коллекция не изменилась");
                    }

                    @Override
                    public void describe() {
                        System.out.println("Дополняет коллекцию элементами из файла с сервера.");
                    }
                },
                new Command("wait", 0) {
                    @Override
                    public void execute() {
                        long start= System.currentTimeMillis();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("All time slept: "+(System.currentTimeMillis()-start));
                    }
                    @Override
                    public void describe() {
                        System.out.println("Создает задержку.");
                    }
                },
                new Command("import", -1) {
                    @Override
                    public void execute() {
                        if (story.getNoises().addAll(story.createNoise(getArguments())))
                            System.out.println("Коллекция изменена");
                        else System.out.println("Коллекция не изменилась");
                    }

                    @Override
                    public void describe() {
                        System.out.println("Дополняет коллекцию элементами из файла клиента.");
                    }
                },
                new Command("update",0) {
                    @Override
                    public void execute() {
                        System.out.println("Колекция обновлена");
                    }
                    @Override
                    public void describe() {
                        System.out.println("Отправляет клиенту коллекцию");
                    }
                });
    }

    private void listen() throws IOException {
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            buffer.clear();
            InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);

            ResponseThread responseThread = new ResponseThread(channel, clientAddress, buffer, story);
            responseThread.start();
        }
    }

    public static void main(String[] args) {
        if(args.length>0) {
            try {
                Server server = new Server(Integer.valueOf(args[0]));
                server.story.getCommandsManager().doCommand("load save.json");
                server.listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else System.out.println("Неправильный запуск. Нужно указать Порт");
    }
}