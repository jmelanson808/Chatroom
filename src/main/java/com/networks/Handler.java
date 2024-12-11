package com.networks;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class Handler
{
	/**
	 * this method is invoked by a separate thread
	 */
	@SuppressWarnings("InfiniteLoopStatement")
	public void process(Socket client, Map<String, UserInfo> socketWriters, ArrayList<Message> messageQueue) throws IOException {
		Pattern joinPattern = Pattern.compile("^JOIN (?!all$)[A-Za-z0-9]{3,30}\n$");
		Pattern leavePattern = Pattern.compile("^LEAVE\n$");
		Pattern sendPattern = Pattern.compile("^SEND \\{.{1,500}}\n$");
        Pattern headerPattern = Pattern.compile("^@[A-Za-z0-9]{3,30}(?: @[A-Za-z0-9]{3,30} ?)*$");
        Pattern timestampPattern = Pattern.compile("^[0-9]{2}:[0-9]{2}$");
        Pattern userBoardPattern = Pattern.compile("^USERBOARD\n$");

        StringBuilder messageBuilder;

        try (BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			 BufferedWriter toClient = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

            while (true) {
                messageBuilder = new StringBuilder();
                int ch;
                while ((ch = fromClient.read()) != -1) {
                    messageBuilder.append((char) ch);
                    if ((char) ch == '\n') {
                        break;
                    }
                }

                String message = messageBuilder.toString();
                System.out.println("Message read: " + message);

                if (joinPattern.matcher(message).matches()) {
                    System.out.println(message.substring(5).trim());
                    if (socketWriters.containsKey(message.substring(5).trim())) {
                        toClient.write("400 INVALID USERNAME\n");
                        toClient.flush();
                    } else {
                        toClient.write("200 OK\n");
                        toClient.flush();
                        System.out.println("Request matches JOIN protocol");

                        socketWriters.put(parseUsername(message), new UserInfo(toClient, UserStatus.ONLINE));
                        socketWriters.forEach((key, value) -> System.out.println("User added: " + key + " " + value));
                    }
                } else if (leavePattern.matcher(message).matches()) {
                    toClient.write("200 BYE\n");
                    toClient.flush();
                    System.out.println("Request matches LEAVE protocol");

                    socketWriters.remove(parseUsername(message));
                    toClient.close();
                    client.close();
                } else if (userBoardPattern.matcher(message).matches()) {
                    System.out.println("Request matches USERBOARD protocol");
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> userBoard = new HashMap<>();

                    socketWriters.forEach((user, info) -> {
                        userBoard.put(user, String.valueOf(info.status()));
                    });

                    String userBoardJson = mapper.writeValueAsString(userBoard);
                    System.out.println("Userboard response: " + userBoardJson);

                    toClient.write("200 BOARD " + userBoardJson + "\n");
                    toClient.flush();
                } else if (sendPattern.matcher(message).matches()) {
                    ObjectMapper mapper = new ObjectMapper();

                    String jsonBody = message.substring(5).trim();
                    System.out.println("JSON request :" + jsonBody);

                    Message sendRequest = mapper.readValue(jsonBody, Message.class);
                    System.out.println("Mapped request: " + sendRequest);

                    if (headerPattern.matcher(sendRequest.header()).matches()) {
                        toClient.write("200 SENT\n");
                        toClient.flush();
                        System.out.println("Request matches SEND protocol");

                        messageQueue.add(sendRequest);
                        System.out.println("Message added: " + sendRequest);
                    } else {
                        toClient.write("400 MESSAGE FAILED\n");
                        toClient.flush();
                    }
                } else {
                    toClient.write("500 SERVER ERROR\n");
                    toClient.flush();
                    System.out.println("The request did not match any protocol.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e);
        } finally {
            if (client != null && !client.isClosed()) {
                System.out.println("Closing server socket for client: " + client.getInetAddress());
                client.close();
            }
        }
	}

	private String parseUsername(String message) {
		return message.substring(5).trim();
	}
}