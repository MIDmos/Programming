package clientSpammer;

import client.Client;

import java.io.IOException;
import java.util.Scanner;

public class Spammer {

    private static final int SPAMMERS_COUNT=20;
    private static  int n;
    private static String addr;
    private static int port;


    private static long start;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Группа спамеров готова к работе.\nУкажите цель");

        //Запрашиваем адрес сервера
        System.out.print("Адрес: ");
        addr=scanner.nextLine();

        //И порт
        System.out.print("Порт: ");
        port=scanner.nextInt();

        start=System.currentTimeMillis();
        for(char c=0;c<SPAMMERS_COUNT;c++){
            SpamThread t = new SpamThread(addr,port,c);
            t.start();
        }
    }
    public static void ended(){
        if(++n==SPAMMERS_COUNT){
            System.out.println("Время исполнения "+(System.currentTimeMillis()-start));
        }
    }
}
class SpamThread extends  Thread{

    private Client client;

    private int port;
    private String address;
    private int c;

    public SpamThread(String address,int port, char c) {
        this.c=c;
        this.address=address;
        this.port=port;
    }

    @Override
    public void run() {
        try {
            System.out.println("Клиент со звуком "+c);
            client = new Client(address,port);
            client.spamSound(c);
        }catch (IOException e ) {
            e.printStackTrace();
        }
        Spammer.ended();
    }
}
