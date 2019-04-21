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
    private LinkedHashSet<Noise>noises;

    public Client(String serverAddress, int port) throws IOException {
        //Устанавливаем флаг работы
        working=true;


        //Создаем сокет и соединяем его с сервером
        datagramSocket = new DatagramSocket();
        datagramSocket.connect(InetAddress.getByName(serverAddress),port);

        //Устанавливаем максимальное время ожидания ответа
        datagramSocket.setSoTimeout(1428);

        scanner = new Scanner(System.in);
        noises=new LinkedHashSet<>();
    }

    /**
     * Метод для проверки работоспособности сервера. За 10 секунд Проверяет сервер 7 раз.
     * Если ответ не был получен, завершает работу клиента
     */
    public void testServerConnection() throws IOException {

        System.out.print("Подключение к серверу ");

        //Запрос для проверки сервера
        DatagramPacket check = createDPacket("connect");

        //Датаграма для ответа
        byte[] buf = new byte[1024];
        DatagramPacket testResponse = new DatagramPacket(buf, buf.length);

        //Флаг подключения
        boolean connected = false;

        String connectString="";

        //Ждем 10 секунд (7 итераций)
        for (int i = 0; i < 7; i++) {
            datagramSocket.send(check);
            try {
                datagramSocket.receive(testResponse);
            } catch (SocketTimeoutException e) {

                //Если время ожидания истекло, и ответ не был получен
                //То мы ждем еще
                System.out.print('.');
                continue;
            }

            //Попадаем сюда, если получили ответ от сервера
            try(ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                ObjectInputStream ois = new ObjectInputStream(bais)){

                //Десериализуем Ответ и выводим сообщение от сервера
                Response response = (Response)ois.readObject();
                connectString = response.getDoings();
                System.out.println(connectString);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            //Если сообщение от сервера соответсвует нашим ожиданиям
            //То мы будем с ним работать
            if (connectString.equals("подключено")) {
                connected = true;
                break;
            }
        }
        System.out.println();

        if (connected) {
            System.out.println("Соединение установлено");
        } else {
            System.out.println("Не удается соединиться с сервером");
            working=false;
        }

    }

    /**
     * Метод работы. Читает команды в строковом формате,
     * создает объект команды по описанию и отправляет его на сервер
     */
    private void work() throws IOException {

        //Выведем информацию о работоспособности
        if (working) System.out.println("Клиент готов к работе");
        else System.out.println("Клиент не может работать");

        while (working){
            System.out.println();
            System.out.print("Ваша команда: ");
            String stringIn=scanner.nextLine().trim();
            System.out.println();

            doCommand(stringIn);
        }
        System.out.println("Сенас завершен.");
    }


    /**
     * Метод для создания датаграм
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

        switch (command.getNAME()){

            //Команда для выхода
            case "exit":
                if(command.getARGS_COUNT()==0)
                    working=false;
                break;

            //При чтении файла нужно передать его содержимое
            //Чем мы и занимаемся
            case "import":
                if(command.getARGS_COUNT()==1){

                    char[] buf=new char[1024];

                    try(FileReader fr = new FileReader(command.getArguments())) {

                        fr.read(buf);
                        String json=String.valueOf(buf);
                        command.setArguments(json);

                    }catch (FileNotFoundException e){
                        System.err.println("Файл не найден");
                        command=null;
                    }
                }
                break;

            //Команда для чтения коллекции, сохраненной у клиента
            case "my":
                noises.forEach(System.out::println);
                if(noises.size()==0)
                    System.out.println("Коллекция пуста");
                command=null;
                break;
        }

        if(command==null)return null;


        //Не null-команду нужно отправить

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)){

            oos.writeObject(command);
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
     * Метод для создания нагрузки на сервер
     * @param c Звук, которым будет спамить клиент будет записан как soundc.
     *          Звук нужен для проверки корректности работы программы.
     */
    public void spamSound(int c) throws IOException{
        String sound="sound"+c;

        //Выводим ознакомительную информацию
        System.out.println("Спамлю с зуком "+sound);

        //Если наш звук максимальный, то в коллекции останется только он.
        //Команда wait создаст задержку на сервере для проверки эффективности многопоточности
        doCommand(String.format("add_if_max {\"%s\":\"%s\"}", sound, sound));
        doCommand("wait");
        doCommand(String.format("remove_lower {\"%s\":\"%s\"}", sound, sound));

        doCommand("exit");
    }

    /**
     * Метод для выполнения команды, заданной в строковом формате
     * @param command Представление команды в виде строки
     */
    private void doCommand(String command)throws IOException{

        //Нам потребуется датаграма для отправки запроса
        DatagramPacket senderDPacket = createDPacket(command);

        //Если команду нужно отправить
        if(senderDPacket!=null) {
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

                    //Выводим сообщение от сервера
                    System.out.println(response.getDoings());
                    //Обновляем коллекцию
                    noises = response.getNoises();

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
    }

    public static void main(String[] args) {

        //При запуске нужно узнать адрес сервера
        String address;
        int port;

        //Через аргумены
        if(args.length==2) {
            address=args[0];
            port=Integer.valueOf(args[1]);
        }
        else{
            //Либо через поток ввода
            Scanner scanner = new Scanner(System.in);

            //Запрашиваем адрес сервера
            System.out.print("Адрес: ");
            address=scanner.nextLine();

            //И порт
            System.out.print("Порт: ");
            port=scanner.nextInt();
        }
        try {
            //Создаем клиента
            Client client = new Client(address,port);
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