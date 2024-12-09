package com.networks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {
    public static final int DEFAULT_PORT = 8000;
    private static final Executor exec = Executors.newCachedThreadPool();

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException {
        Map<String, UserInfo> socketWriters;
        ArrayList<Message> messageQueue;
        ServerSocket sock = null;

        try {
            socketWriters = new HashMap<>();
            messageQueue = new ArrayList<>();
            sock = new ServerSocket();
            sock.bind(new InetSocketAddress("146.86.108.47", DEFAULT_PORT));

            Thread broadcaster = new Thread(new BroadcastThread(socketWriters, messageQueue));
            broadcaster.start();

            while (true) {
                System.out.println("Listening for new connections...");

                Socket client = sock.accept();
                System.out.println("Client connected: " + client.getInetAddress());

                Runnable task = new Connection(client, socketWriters, messageQueue);
                exec.execute(task);
                socketWriters.forEach((key, value) -> System.out.println("User removed: " + key + " " + value));
            }
        } catch (IOException ioe) {
            System.err.println("Server Exception " + ioe);
        } finally {
            if (sock != null)
                sock.close();
        }
    }
}
