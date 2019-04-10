package client;

import java.io.IOException;

public class SpammerClients {
    public static void main(String[] args) {
        long start =System.currentTimeMillis();
        System.out.println("Начало в "+System.currentTimeMillis());
        for(char c=args[0].charAt(0);c<=args[1].charAt(0);c++){
            SpamThread t = new SpamThread(c);
            t.start();
        }
        long end =System.currentTimeMillis();
        System.out.println("Конец в "+System.currentTimeMillis());
        System.out.println("Всего времени "+(end-start));
    }
}
class SpamThread extends  Thread{
    private Client client;
    private char c;

    public SpamThread( char c) {
        this.c=c;
    }

    @Override
    public void run() {
        try {
            System.out.println("Клиент со звуком "+c);
            client = new Client("localhost",25565);
            client.spamSound(c);
        }catch (IOException e ) {
            e.printStackTrace();
        }
    }
}
