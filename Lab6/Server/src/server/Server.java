package server;

import fairytale.FileHandler;
import fairytale.Noise;
import fairytale.Story;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;
import java.util.Set;

public class Server {

    private Set<Noise> noises;
    private DatagramChannel channel;


    public Server(int port) throws IOException {
        //Открываем новый канал
        channel = DatagramChannel.open().
                //Связываем сокет канала с адресом
                bind(new InetSocketAddress(InetAddress.getLocalHost(), port));

        //Загружаем коллекцию шумов
        Story story = new Story();
        if (story.getNoises().addAll(FileHandler.readFile("save.json")))
            System.out.println("Коллекция изменена");
        else System.out.println("Коллекция не изменилась");

        noises=story.getNoises();

        //Выводим информацию о сервере
        System.out.println("Сервер запущен");
        System.out.println("IP: " + channel.getLocalAddress());


    }

    private void listen() throws IOException {
        while (true) {
            //Создаем буфер для хранения датаграмы
            ByteBuffer buffer = ByteBuffer.allocate(4096);

            //Копируем полученную датаграмму в буфер
            //И сохраняем адрес отправителя
            InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);

            //Создаем поток для обработки запроса
            ResponseThread responseThread = new ResponseThread(channel, clientAddress, buffer, noises);
            //Запускаем его
            responseThread.start();
        }
    }

    public static void main(String[] args) {

        //При запуске сервера нужно задать порт
        int port = 0;

        if(args.length>0) {
            port = Integer.valueOf(args[0]); //Либо в аргументах
        }else {
            Scanner scanner = new Scanner(System.in); //Либо через поток ввода
            System.out.print("Введите порт: ");
            port=scanner.nextInt();
        }
        try {
            Server server = new Server(port);  //Создаем сервер
            server.listen();        //Запускаем прослушку запросов
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}