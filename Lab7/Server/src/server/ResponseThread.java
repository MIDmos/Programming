package server;

import client.Request;
import fairytale.FileHandler;
import fairytale.Noise;
import fairytale.Story;
import fairytale.commands.Command;
import fairytale.commands.CommandDescriptor;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashSet;

public class ResponseThread extends Thread {

    private final InetSocketAddress clientAddress;
    private final ServerCommandManager manager;
    private final Story story;
    private Response.State state;
    private String login;
    private String token;
    private long id;
    private ByteBuffer buffer;
    private Connection dbConnection;
    private DatagramChannel channel;

    public ResponseThread(Server server,DatagramChannel channel, InetSocketAddress clientAddress, ByteBuffer buffer, Connection connection){

        state= Response.State.UNKNOWN;

        dbConnection=connection;


        HashSet<Noise> noises = new HashSet<>();
        try {
            Statement statement = dbConnection.createStatement();

            ResultSet resultSet = statement.executeQuery(QueryStorage.SELECT_NOISES);
            while (resultSet.next()){
                noises.add(new Noise(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Создаем класс для работы с коллекцией
        story=new Story();

        //Загружаем в него коллекцию с сервера
        //story.setNoises(noises);

        //Добавляем в обработчика серверные команды
        manager=new ServerCommandManager(dbConnection,server,token,id,noises);
        manager.addCommand(
                new Command("register", 1) {
                    @Override
                    public void execute() {
                        try {
                            //Check if user is not registered
                            if(!loginExsists()) {
                                //Send email
                                InternetAddress user = new InternetAddress(getArguments());
                                user.validate();

                                String password = RandomPassword.createPassword();

                                EmailHandler.sendEmail(user, "Регистрация в финансовой пирамиде", "Ваш логин: "+login+"\nВаш пароль : " + password);
                                manager.println("Пароль для авторизации выслан на почту");

                                //add new row to table
                                PreparedStatement statement = dbConnection.prepareStatement(QueryStorage.ADD_USER);
                                statement.setString(1, getArguments());
                                statement.setString(2, login);
                                statement.setString(3, securePassword(password));
                                statement.executeUpdate();
                            }else {
                                manager.println("Пользователь с таким логином уже зарегистрирован");
                            }

                        } catch (SQLException e) {
                            manager.println(e.getMessage());
                        }catch (AddressException e) {
                            manager.println("Плохой адрес");
                        }
                    }
                    @Override
                    public void describe() {
                        manager.println("Команда для регистрации пользователя");
                    }
                },
                new Command("login", 1) {
                    @Override
                    public void execute() {
                        try {
                            if(loginExsists()) {
                                if(token.equals("")||!(server.userLogined(token))) {
                                    PreparedStatement statement = dbConnection.prepareStatement(QueryStorage.CHECK_PASSWORD);
                                    statement.setString(1, login);
                                    statement.setString(2, securePassword(getArguments()));
                                    ResultSet resultSet = statement.executeQuery();
                                    if (resultSet.next()) {
                                        manager.println("Вход разрешен");
                                        token=securePassword(login+getArguments());
                                        id=resultSet.getInt("user_id");
                                        server.addUser(token);
                                        state = Response.State.LOGINED;
                                    } else {
                                        manager.println("Неверный пароль");
                                    }
                                }else{
                                    manager.println("Пользователь уже активен");
                                }
                            }else {
                                manager.println("Вы не зарегистрированы");
                                manager.println("Для регистрации введите команду register (ваша почта)");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void describe() {
                        manager.println("Команда для регистрации пользователя");
                    }
                },
                new Command("connect", 0) {
                    @Override
                    public void execute() {
                        state= Response.State.CONNECTED;
                        manager.println("Подключено");
                        try {
                            if(loginExsists()) {
                                //If login is not active
                                if(!server.userLogined(token)) {
                                    manager.println();
                                    manager.println("Введите команду login (ваш пароль)");

                                    PreparedStatement statement = dbConnection.prepareStatement(QueryStorage.FIND_USER_BY_LOGIN);
                                    statement.setString(1,login);
                                    ResultSet resultSet = statement.executeQuery();
                                    resultSet.next();
                                    id=resultSet.getInt("user_id");
                                }
                                else {
                                    token="-1";
                                    manager.println("Пользователь с таким логином сейчас активен.");
                                    manager.println("Возможно, вам стоит зарегистрироваться под другим логином.");
                                }
                            }else{
                                manager.println("Вы не зарегистрированы.");
                                manager.println();
                                manager.println("Для регистрации введите команду register (ваша почта)");
                            }
                        } catch (SQLException e) {
                            manager.println(e.getMessage());
                        }
                    }
                    @Override
                    public void describe() {
                        manager.println("Команда для установки соединения");
                    }
                },
                new Command("wait", 0) {
                    @Override
                    public void execute() {
                        if (manager.loginConfirmed()) {
                            long start = System.currentTimeMillis();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            manager.println(Thread.currentThread().getId() + " ждал " + (System.currentTimeMillis() - start));
                        }
                    }
                    @Override
                    public void describe() {
                        manager.println("Создает задержку.");
                    }
                },
                new Command("import", -1) {
                    @Override
                    public void execute() {
                        if (manager.loginConfirmed()) {
                            Noise.fromJson(manager.getId(),getArguments()).forEach(noise -> {
                                if(story.getNoises().add(noise)){
                                    manager.addNoise(noise);
                                    manager.println(noise + " добавлен");
                                }else
                                    manager.println(noise+" не добавлен");
                            });
                        }
                    }
                    @Override
                    public void describe() {
                        manager.println("Дополняет коллекцию элементами из файла клиента.");
                    }
                },
                new Command("update",0) {
                    @Override
                    public void execute() {
                        if(manager.loginConfirmed())
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

    private String securePassword(String password){
        String md5=null;
        try {
            byte[] bytesOfMessage = password.getBytes();

            //Хэшируем пароль
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] b = md.digest(bytesOfMessage);

            //Создаем шестнадцатиричную строку из хэша
            StringBuilder sb = new StringBuilder(32);
            for(byte b1: b)
                sb.append(String.format("%02x", b1));

            //Эту строку и вернем
            md5=sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    private boolean loginExsists() throws SQLException{
        PreparedStatement statement = dbConnection.prepareStatement(QueryStorage.FIND_USER_BY_LOGIN);
        statement.setString(1,login);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public void run() {

        //Команда, которую надо выполнить
        CommandDescriptor command;

        //Берем массив байт из буфера
        byte[] requestBuf=buffer.array();

        try (//Потоки для чтения команды
             ByteArrayInputStream bais = new ByteArrayInputStream(requestBuf);
             ObjectInputStream ois = new ObjectInputStream(bais);
             //Потоки для записи ответа
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);
             //Потоки для записи сообщений
             ByteArrayOutputStream bao = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(bao)) {


            //Читаем запрос клиента
            Request request = (Request) ois.readObject();
            command = request.getDESCRIPTOR();
            login=request.getLOGIN();
            token = request.getTOKEN();
            id = request.getID();
            manager.setToken(token);
            manager.setId(id);
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
            Response response = new Response(token,doings,manager.getNoises(),state,id);

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