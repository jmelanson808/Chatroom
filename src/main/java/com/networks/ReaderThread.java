package com.networks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

public class ReaderThread implements Runnable
{
	Socket server;
	BufferedReader fromServer;
	ChatScreen screen;
	String user;
	ObjectMapper mapper = new ObjectMapper();

	public ReaderThread(Socket server, ChatScreen screen, String user) {
		this.server = server;
		this.screen = screen;
		this.user = user;
	}

	@SuppressWarnings("BusyWait")
	public void run() {
		try {
			fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));

			while (true) {
				String message = "";
				System.out.println("Reader thread is running....");

				String response = fromServer.readLine();
				System.out.println("clientReader: " + response);

				if (response == null) {
					System.out.println("Connection closed. Closing Reader thread."); break;
				}

				switch(response) {
					case "200 OK":
						screen.displayMessage("Welcome to the Chatroom " + user + "!"); break;
					case "200 BYE":
						screen.displayMessage("Logging you out. Have a great day!");
						Thread.sleep(1000);
						System.exit(0);
						break;
					case "200 SENT":
						break;
					case "200 BOARD":
						// TODO: How will I be able to get both the 200 BOARD and the json response?
						Map<String, UserStatus> incomingBoard = mapper.readValue(response, new TypeReference<Map<String, UserStatus>>() {});

						StringBuilder messageBuilder = new StringBuilder();
						incomingBoard.forEach((user, status) -> {
							messageBuilder.append(user).append(" : ").append(status).append("\n");
						});

						screen.displayMessage("Current Users\n" + messageBuilder);
					case "400 INVALID USERNAME":
						screen.displayMessage("Username has been taken.");
						Thread.sleep(1000);
						System.exit(0);
						break;
					case "400 MESSAGE FAILED":
						screen.displayMessage("Your message was not sent. It did not have the required structure.\n" +
								              "Try again with the form @<username> <message>"); break;
					case "500 SERVER ERROR":
						screen.displayMessage("Something went wrong"); break;
					default:
						Message incomingMessage = mapper.readValue(response, Message.class);
						message = incomingMessage.sender() + " : " + incomingMessage.message() + " (" + incomingMessage.timestamp() + ")";
				}

				screen.displayMessage(message);
			}
		}
		catch (IOException | InterruptedException ioe) {
			System.err.println("ReaderThread Exception: " + ioe);
		} finally {
			try {
				if (fromServer != null) {
					fromServer.close();
				}
				if (server != null) {
					server.close();
				}
			} catch (IOException e) {
				System.err.println("Error closing resources: " + e.getMessage());
			}
		}
	}
}