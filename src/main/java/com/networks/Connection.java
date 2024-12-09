package com.networks;

import java.io.BufferedWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class Connection implements Runnable
{
	private final Socket client;
	private static final Handler handler = new Handler();
	private final Map<String, UserInfo> socketWriters;
	private final ArrayList<Message> messageQueue;

	public Connection(Socket client, Map<String, UserInfo> socketWriters, ArrayList<Message> messageQueue) {
		this.client = client;
		this.socketWriters = socketWriters;
		this.messageQueue = messageQueue;
	}

	/**
	 * This method runs in a separate thread.
	 */
	public void run() {
		try {
			handler.process(client, socketWriters, messageQueue);
		}
		catch (java.io.IOException ioe) {
			System.err.println("Connection exception: " + ioe);
		}
	}
}