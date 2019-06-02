package server;

import fairytale.FileHandler;
import fairytale.Story;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Server {

    private static final String DB_DRIVER="org.postgresql.Driver";  //Драйвер

    //Use "jdbc:postgresql://localhost:5432/studs" to start server on local device
    private static final String DB_CONNECTION="jdbc:postgresql://pg:5432/studs"; //URI для к ДБ (запуск на хелиосе)

    private Connection dbConnection;
    private DatagramChannel channel;
    private final Set<String> users;
    public final long INIT_TIME;


    public Server(int port, String user, String password) throws IOException {
        //Открываем новый канал
        channel = DatagramChannel.open().
                //Связываем сокет канала с адресом
                bind(new InetSocketAddress(InetAddress.getLocalHost(), port));


        //Подключаемся к бд
        try {
            //Указываем драйвер
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver error");
            System.out.println(e.getMessage());
        }
        //Устанавливаем соединение с БД, испльзуя DriverManager
        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, user,password);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(1);
        }

        //Create new users set
        users = Collections.synchronizedSet(new HashSet<>());


        //Загружаем коллекцию шумов
        Story story = new Story();
        if (story.getNoises().addAll(FileHandler.readFile("save.json")))
            System.out.println("Коллекция изменена");
        else System.out.println("Коллекция не изменилась");

        //Выводим информацию о сервере
        System.out.println("Сервер запущен");
        System.out.println("IP: " + channel.getLocalAddress());

        INIT_TIME=System.currentTimeMillis();
    }

    private void listen() throws IOException {
        while (true) {
            //Создаем буфер для хранения датаграмы
            ByteBuffer buffer = ByteBuffer.allocate(4096);

            //Копируем полученную датаграмму в буфер
            //И сохраняем адрес отправителя
            InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);

            //Создаем поток для обработки запроса
            ResponseThread responseThread = new ResponseThread(this, channel, clientAddress, buffer, dbConnection);
            //Запускаем его
            responseThread.start();
        }
    }

    public static void main(String[] args) {

        int port;
        String user,password;
        //Используем консоль для безопасного ввода пароля
        Console console = System.console();


        //Для безопасности
        if(console!=null) {
            console.printf("Введите порт: ");
            port = Integer.valueOf(console.readLine());
            console.printf("Введите имя пользователя: ");
            user = console.readLine();
            console.printf("Введите пароль: ");
            password = String.valueOf(console.readPassword());
        }else{
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите порт: ");
            port=scanner.nextInt();
            System.out.print("Введите имя пользователя: ");
            user = scanner.next().trim();
            System.out.print("Введите пароль: ");
            password = scanner.next().trim();
        }

        try {
            Server server = new Server(port,user,password);  //Создаем сервер
            server.listen();        //Запускаем прослушку запросов
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized boolean addUser(String token){
        return users.add(token);
    }
    public synchronized boolean userLogined(String token){
        return users.contains(token);
    }

    public synchronized boolean removeUser(String token){
        return  users.remove(token);
    }

    public synchronized Set<String> getUsers() {
        return users;
    }
}