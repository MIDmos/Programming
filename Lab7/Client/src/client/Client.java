package client;

import fairytale.Noise;
import fairytale.commands.CommandDescriptor;
import server.Response;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedHashSet;
import java.util.Scanner;

public class Client {
    private DatagramSocket datagramSocket;
    private Scanner scanner;
    private boolean working;
    private LinkedHashSet<Noise> noises;
    private final String LOGIN;
    private String token;
    private long id;

    public Client(String login, String serverAddress, int port) throws IOException {

        //Задаем логин пользователя
        LOGIN = login;

        //Token is used to identify user on server
        token="";

        //Устанавливаем флаг работы
        working = true;


        //Создаем сокет и соединяем его с сервером
        datagramSocket = new DatagramSocket();
        datagramSocket.connect(InetAddress.getByName(serverAddress), port);

        //Устанавливаем максимальное время ожидания ответа
        datagramSocket.setSoTimeout(4000);

        scanner = new Scanner(System.in);
        noises = new LinkedHashSet<>();

    }


    /**
     * Метод для проверки работоспособности сервера. За 10 секунд Проверяет сервер 7 раз.
     * Если ответ не был получен, завершает работу клиента
     */
    public void testServerConnection() throws IOException {

        System.out.print("Подключение к серверу ");

        //Запрос для проверки сервера
        DatagramPacket check = createDPacket("connect");

        int timeout = datagramSocket.getSoTimeout();
        datagramSocket.setSoTimeout(1428);
        //Датаграма для ответа
        byte[] buf = new byte[1024];
        DatagramPacket testResponse = new DatagramPacket(buf, buf.length);

        //Флаг подключения
        boolean connected = false;

        String connectString = "";

        //Ждем 10 секунд (7 итераций)
        for (int i = 0; i < 7; i++) {
            datagramSocket.send(check);
            try {
                datagramSocket.receive(testResponse);
            } catch (SocketTimeoutException e) {

                //Если время ожидания истекло, и отвеdт не был получен
                //То мы ждем еще
                System.out.print('.');
                continue;
            }

            //Попадаем сюда, если получили ответ от сервера
            Response response = null;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {

                //Десериализуем Ответ и выводим сообщение от сервера
                response = (Response) ois.readObject();
                connectString = response.getDoings();
                System.out.println();
                System.out.println(connectString);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            //Если сообщение от сервера соответсвует нашим ожиданиям
            //То мы будем с ним работать
            if (response.getState()==Response.State.CONNECTED) {
                connected = true;
                break;
            }
        }
        datagramSocket.setSoTimeout(timeout);
        System.out.println();

        if (connected) {
            System.out.println("Соединение установлено");
        } else {
            System.out.println("Не удается соединиться с сервером");
            working = false;
        }
    }

    /**
     * Метод работы. Читает команды в строковом формате,
     * создает объект команды по описанию и отправляет его на сервер
     */
    private void work() throws IOException {

        //Выведем информацию о работоспособности
        if (!working) System.out.println("Клиент не может работать");

        while (working) {
            System.out.println();
            System.out.print("Ваша команда: ");
            String stringIn = scanner.nextLine().trim();
            System.out.println();

            Response response = doCommand(stringIn);

            if(response!=null) {
                //refresh token when login
                if(response.getState()== Response.State.LOGINED) {
                    token = response.getToken();
                    id = response.getId();
                }
                //Выводим сообщение от сервера
                System.out.println(response.getDoings());
                //Обновляем коллекцию
                noises = response.getNoises();
            }
        }
        System.out.println("Сенас завершен.");
    }


    /**
     * Метод для создания датаграм
     *
     * @param description Команда, заданная в формате строки
     * @return Возвращает Команду, завернутую в Датаграму, если команду нельзя выполнить на стороне клиента.
     * В противном случае возвращает null
     */
    private DatagramPacket createDPacket(String description) throws IOException {

        //Массив байтов - данные для датаграмы
        byte[] sending;

        //Команду будем передавать, используя Дескриптор
        CommandDescriptor command = new CommandDescriptor(description);


        //Если команда не предназначена для отправки на сервер, присвоим ей значение null

        switch (command.getNAME()) {

            //Команда для выхода
            case "exit":
                if (command.getARGS_COUNT() == 0)
                    working = false;
                break;
            case "login":
                if (command.getARGS_COUNT()==1)
                    token="";
                break;

            //При чтении файла нужно передать его содержимое
            //Чем мы и занимаемся
            case "import":
                if (command.getARGS_COUNT() == 1) {

                    char[] buf = new char[1024];

                    try (FileReader fr = new FileReader(command.getArguments())) {

                        fr.read(buf);
                        String json = String.valueOf(buf);
                        command.setArguments(json);

                    } catch (FileNotFoundException e) {
                        System.err.println("Файл не найден");
                        command = null;
                    }
                }
                break;

            //Команда для чтения коллекции, сохраненной у клиента
            case "my":
                noises.forEach(System.out::println);
                if (noises.size() == 0)
                    System.out.println("Коллекция пуста");
                command = null;
                break;
        }

        if (command == null) return null;


        //Не null-команду нужно отправить

        Request request = new Request(id,token,LOGIN, command);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(request);
            oos.flush();
            sending = baos.toByteArray();
            //Упаковываем ее в массив байтов

            //И создаем датаграму
            return new DatagramPacket(sending, sending.length);
        } catch (IOException e) {
            throw e;
        }
    }


    /**
     * Метод для выполнения команды, заданной в строковом формате
     *
     * @param command Представление команды в виде строки
     */
    private Response doCommand(String command) throws IOException {

        //Нам потребуется датаграма для отправки запроса
        DatagramPacket senderDPacket = createDPacket(command);

        //Если команду нужно отправить
        if (senderDPacket != null) {
            //Отправляем ее
            datagramSocket.send(senderDPacket);

            //Создаем датаграму - ответ сервера
            byte[] respBuf = new byte[4096];
            DatagramPacket responsePacket = new DatagramPacket(respBuf, respBuf.length);

            try {
                //Ждем ответ
                datagramSocket.receive(responsePacket);

                try (ByteArrayInputStream bais = new ByteArrayInputStream(respBuf);
                     ObjectInputStream ois = new ObjectInputStream(bais)) {

                    //Десериализуем Ответ
                    Response response = (Response) ois.readObject();


                    return response;

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            } catch (SocketTimeoutException e) {
                //Если ждем слишком долго, то отключаемся
                System.err.println("Превышено время ожидания");
                System.err.println("Разрыв соединения");
                testServerConnection();
            }
        }
        return null;
    }

    public static void main(String[] args) {


        //При запуске нужно указать логин и адрес сервера
        String login;
        String address;
        int port;

        //Через аргумены
        if (args.length == 3) {
            login = args[0];
            address = args[1];
            port = Integer.valueOf(args[2]);
        } else {
            //Либо через поток ввода
            Scanner scanner = new Scanner(System.in);

            //Запрашиваем логин
            System.out.print("Логин: ");
            login = scanner.nextLine();

            //Запрашиваем адрес сервера
            System.out.print("Адрес: ");
            address = scanner.nextLine();

            //И порт
            System.out.print("Порт: ");
            port = scanner.nextInt();
        }
        try {
            //Создаем клиента
            Client client = new Client(login, address, port);
            System.out.println("Добро пожаловать.");

            //Проверяем работоспособность сервера
            client.testServerConnection();

            //Начинаем работать
            client.work();

        } catch (Exception e) {
            System.err.println("Возникла ошибка. Отклчаюсь.");
            e.printStackTrace();
        }
    }
}