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
    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int port;
    private Scanner scanner;
    private boolean working;
    private LinkedHashSet<Noise>noises;

    public Client(String serverAddress, int port) throws IOException {
        working=true;
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.port = port;
        udpSocket = new DatagramSocket();
        scanner = new Scanner(System.in);
        noises=new LinkedHashSet<>();
    }

    public void testServerConnection() throws IOException {
        System.out.print("Подключение к серверу ");
        DatagramPacket check = createDPacket("connect");

        byte[] buf = new byte[1024];
        DatagramPacket testResponse = new DatagramPacket(buf, buf.length);

        boolean connected = false;
        udpSocket.setSoTimeout(1428);
        String connectString="";
        for (int i = 0; i < 7; i++) {
            udpSocket.send(check);
            try {
                udpSocket.receive(testResponse);
            } catch (SocketTimeoutException e) {
                System.out.print('.');
                continue;
            }
            try(ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                ObjectInputStream ois = new ObjectInputStream(bais)){

                Response response = (Response)ois.readObject();
                connectString = response.getDoings();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (connectString.equals("подключено")) {
                connected = true;
                break;
            }
        }
        System.out.println();

        if (connected) {
            System.out.println("Соединение установлено");
        } else {
            System.err.println("Не удается соединиться с сервером");
            System.exit(1);
        }

    }

    private void work() throws IOException {
        System.out.println("Клиент готов к работе");
        DatagramPacket senderDPacket;

        String stringIn;


        while (working){
            System.out.print("Ваша команда: ");
            stringIn=scanner.nextLine().trim();
            doCommand(stringIn);
        }
        System.out.println("Сенас завершен.");
    }


    private DatagramPacket createDPacket(String description) throws IOException {
        byte[] sending;
        CommandDescriptor command = new CommandDescriptor(description);
        switch (command.getNAME()){
            case "exit":
                if(command.getARGS_COUNT()==0)
                    working=false;
                break;
            case "import":
                if(command.getARGS_COUNT()==1){
                    char[] buf=new char[1024];
                    try(FileReader fr = new FileReader(command.getArguments())) {
                        fr.read(buf);
                        String json=String.valueOf(buf);
                        command.setArguments(json);
                    }catch (FileNotFoundException e){
                        System.out.println("Файл не найден");
                        command=null;
                    }
                }
                break;
            case "read":
                for (Noise noise : noises) System.out.println(noise);
                if(noises.size()==0)
                    System.out.println("Коллекция пуста");
                command=null;
                break;

        }
        if(command==null)return null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(outputStream)){
            oos.writeObject(command);
            oos.flush();
            sending = outputStream.toByteArray();
            return new DatagramPacket(sending, sending.length, serverAddress, port);
        } catch (IOException e) {
            throw new IOException();
        }
    }

    public void spamSound(char c) throws IOException{
        System.out.println("Спамлю с зуком "+c);
        String sound="sound"+c;
        //doCommand(String.format("add_if_max {\"%s\":\"%s\"}", sound, sound));
        doCommand("wait");
        //doCommand(String.format("remove_lower {\"%s\":\"%s\"}", sound, sound));
    }
    private void doCommand(String command)throws IOException{
        DatagramPacket senderDPacket;

        senderDPacket = createDPacket(command);
        if(senderDPacket!=null) {
            udpSocket.send(senderDPacket);

            byte[] respBuf = new byte[4096];
            DatagramPacket responsePacket = new DatagramPacket(respBuf, respBuf.length);
            try {
                this.udpSocket.receive(responsePacket);

                try (ByteArrayInputStream bais = new ByteArrayInputStream(respBuf);
                     ObjectInputStream ois = new ObjectInputStream(bais)) {

                    Response response = (Response) ois.readObject();

                    System.out.println(response.getDoings());
                    noises = response.getNoises();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            } catch (SocketTimeoutException e) {
                System.err.println("Превышено время ожидания");
                System.err.println("Разрыв соединения");
                testServerConnection();
            }
        }
    }

    public static void main(String[] args) {
        if(args.length>0) {
            try {
                Client sender = new Client("localhost",Integer.valueOf(args[0]));
                System.out.println("Добро пожаловать.");

                sender.testServerConnection();
                sender.work();
            } catch (Exception e) {
                System.err.println("Возникла ошибка. Отклчаюсь.");
                e.printStackTrace();
            }
        }else System.out.println("Пожалуйста, укажите адресс порт.");
    }
}