package server;

import fairytale.FileHandler;
import fairytale.Noise;
import fairytale.Story;
import fairytale.commands.Command;
import fairytale.commands.CommandDescriptor;
import fairytale.commands.CommandsManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Set;

public class ResponseThread extends Thread {

    private final InetSocketAddress clientAddress;
    private final CommandsManager manager;
    private final Story story;
    private ByteBuffer buffer;
    private DatagramChannel channel;

    public ResponseThread(DatagramChannel channel, InetSocketAddress clientAddress, ByteBuffer buffer, Set<Noise> noises){

        //Создаем класс для работы с коллекцией
        story=new Story();

        //Загружаем в него коллекцию с сервера
        story.setNoises(noises);

        //Добавляем в обработчика серверные команды
        manager=story.getCommandsManager();
        manager.addCommand(
                new Command("connect", 0) {
                    @Override
                    public void execute() {
                        manager.println("подключено");
                    }
                    @Override
                    public void describe() {
                        manager.println("Команда для проверки соединения");
                    }
                },
                new Command("load", 1) {
                    @Override
                    public void execute() {
                        if (story.getNoises().addAll(FileHandler.readFile(getArguments())))
                            manager.println("Коллекция изменена");
                        else manager.println("Коллекция не изменилась");
                    }

                    @Override
                    public void describe() {
                        manager.println("Дополняет коллекцию элементами из файла с сервера.");
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
                        manager.println(Thread.currentThread().getId()+" ждал "+(System.currentTimeMillis()-start));
                    }
                    @Override
                    public void describe() {
                        manager.println("Создает задержку.");
                    }
                },
                new Command("import", -1) {
                    @Override
                    public void execute() {
                        if (story.getNoises().addAll(story.createNoise(getArguments())))
                            manager.println("Коллекция изменена");
                        else manager.println("Коллекция не изменилась");
                    }

                    @Override
                    public void describe() {
                        manager.println("Дополняет коллекцию элементами из файла клиента.");
                    }
                },
                new Command("update",0) {
                    @Override
                    public void execute() {
                        manager.println("Колекция обновлена");
                    }
                    @Override
                    public void describe() {
                        manager.println("Отправляет клиенту коллекцию");
                    }
                });

        //Инициализируем необходимые поля
        this.channel=channel;
        this.clientAddress=clientAddress;
        this.buffer=buffer;
    }

    public void run() {

        //Команда, которую надо выполнить
        CommandDescriptor command;

        //Берем массив байт из буфера
        byte[] request=buffer.array();

        try (//Потоки для чтения команды
             ByteArrayInputStream bais = new ByteArrayInputStream(request);
             ObjectInputStream ois = new ObjectInputStream(bais);
             //Потоки для записи ответа
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);
             //Потоки для записи сообщений
             ByteArrayOutputStream bao = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(bao)) {


            //Читаем команду клиента
            command = (CommandDescriptor) ois.readObject();

            //Назначаем поток вывода для обработчика команд
            manager.setPrintStream(printStream);

            //Выполняем команду
            //В результате ее выполнения будет проведена работа с коллекцией, будет сформировано сообщение для клиента.
            manager.doCommand(command);

            //Очищаем поток
            printStream.flush();

            //Формируем сообщение
            String doings = new String(bao.toByteArray()).trim();

            //Оставляем информацию на сервере
            synchronized (System.out) {
                System.out.println();
                System.out.println("Команда клиента("+Thread.currentThread().getId()+"): " + command.getNAME() + " " + command.getArguments());
                System.out.println();
                //Записываем на сервер сообщение, которое потом пошлем клиенту
                System.out.println(doings);
            }

            //Ответ готов
            Response response = new Response(doings,story.getNoises());

            //Сериализуем его
            oos.writeObject(response);
            oos.flush();

            //И записываем в буфер
            buffer.clear();
            buffer.put(baos.toByteArray());
            buffer.flip();

            //И отправляем
            channel.send(buffer, clientAddress);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}