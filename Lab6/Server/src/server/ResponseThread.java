package server;

import fairytale.Story;
import fairytale.commands.CommandDescriptor;
import fairytale.commands.CommandsManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ResponseThread extends Thread {

    private final InetSocketAddress clientAddress;
    private final CommandsManager manager;
    private final Story story;
    private ByteBuffer buffer;
    private DatagramChannel channel;

    public ResponseThread(DatagramChannel channel,InetSocketAddress clientAddress, ByteBuffer buffer, final Story story){
        super();
        this.story=story;
        manager=story.getCommandsManager();
        this.channel=channel;
        this.clientAddress=clientAddress;
        this.buffer=buffer;
    }

    public void run() {
        CommandDescriptor command;

        byte[] request=buffer.array();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(request);
             ObjectInputStream ois = new ObjectInputStream(bais);

             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);

             ByteArrayOutputStream bao = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(bao)) {

            command = (CommandDescriptor) ois.readObject();

            Response response = null;

            synchronized (story.getNoises()) {

                PrintStream prev = System.out;
                System.setOut(printStream);

                System.out.println();
                manager.doCommand(command);
                System.out.println();

                System.out.flush();
                System.setOut(prev);

                String doing = bao.toString().trim();
                System.out.println("Команда клиента: " + command.getNAME() + " " + command.getArguments());
                System.out.println(doing);

                response = new Response(doing,story.getNoises());
            }

            oos.writeObject(response);
            oos.flush();

            buffer.clear();
            buffer.put(baos.toByteArray());
            buffer.flip();

            channel.send(buffer, clientAddress);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}