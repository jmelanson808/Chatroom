package com.networks;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;


public class BroadcastThread implements Runnable {
    Map<String, UserInfo> socketWriters;
    ArrayList<Message> messageQueue;

    public BroadcastThread(Map<String, UserInfo> socketWriters, ArrayList<Message> messageQueue) {
        this.socketWriters = socketWriters;
        this.messageQueue = messageQueue;
    }

    @SuppressWarnings({ "InfiniteLoopStatement", "BusyWait" })
    public void run() {
        System.out.println("Broadcaster thread is running...");
        ObjectMapper mapper = new ObjectMapper();
        List<String> results;

        while (true) {
            // sleep for 1/10th of a second
            try { Thread.sleep(100); } catch (InterruptedException ignore) { }

            if (!messageQueue.isEmpty()) {
                List<Message> toRemove = new ArrayList<>();
                for (Message message : messageQueue) {
                    if (message.header().contains("@all")) {
                        socketWriters.forEach((user, info) -> {
                            if (!user.equals(message.sender())) {
                                try {
                                    info.writer().write(mapper.writeValueAsString(message) + "\n");
                                    info.writer().flush();
                                    toRemove.add(message);
                                    System.out.println("Broadcast message sent to " + user + ": " + message);
                                } catch (IOException e) {
                                    throw new RuntimeException("Broadcast Exception: " + e);
                                }
                            }
                        });
                    } else {
                        results = Arrays.stream(message.header().trim().split(" "))
                                .map(header -> header.replaceAll("^@", ""))
                                .filter(socketWriters::containsKey)
                                .toList();

                        if (results.isEmpty()) {
                            try {
                                socketWriters.get(message.sender()).writer().write("500 SERVER ERROR");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        results.forEach((result) -> {
                            try {
                                socketWriters.get(result).writer().write(mapper.writeValueAsString(message) + "\n");
                                socketWriters.get(result).writer().flush();
                                toRemove.add(message);
                                System.out.println("Private message sent to " + socketWriters.get(result) + ": " + message);
                            } catch (IOException e) {
                                throw new RuntimeException("Broadcast Exception: " + e);
                            }
                        });
                    }
                }
                messageQueue.removeAll(toRemove);
            }
        }
    }
}
