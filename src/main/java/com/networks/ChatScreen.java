package com.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatScreen extends JFrame implements ActionListener, KeyListener {
    private final JButton sendButton;
    private final JButton exitButton;
    private final JTextField sendText;
    private final JTextArea displayArea;

    private final String username;
    private final BufferedWriter toServer;


    public static final int PORT = 8000;

    public ChatScreen(Socket socket, String username) throws IOException {
        this.username = username;
        this.toServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        JPanel p = new JPanel(); // Panel used for placing components

        Border etched = BorderFactory.createEtchedBorder();
        Border titled = BorderFactory.createTitledBorder(etched, "Enter Message Here ...");
        p.setBorder(titled);

        // Set up all the components
        sendText = new JTextField(30);
        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");

        // Register the listeners for the different button clicks
        sendText.addKeyListener(this);
        sendButton.addActionListener(this);
        exitButton.addActionListener(this);

        // Add the components to the panel
        p.add(sendText);
        p.add(sendButton);
        p.add(exitButton);

        // Add the panel to the "south" end of the container
        getContentPane().add(p, "South");

        /*
         * add the text area for displaying output. Associate
         * a scrollbar with this text area. Note we add the scroll-pane
         * to the container, not the text area
         */
        displayArea = new JTextArea(15, 40);
        displayArea.setEditable(false);
        displayArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(displayArea);
        getContentPane().add(scrollPane, "Center");

        // Set the title and size of the frame
        setTitle("Chatroom");
        pack();

        setVisible(true);
        sendText.requestFocus();

        // Anonymous inner class to handle window closing events
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
    }

    public void displayMessage(String message) {
        displayArea.append(message + "\n");
    }

    /**
     * This gets the text the user entered and outputs it
     * in the display area
     */
    public void displayText() throws IOException {
        String message = sendText.getText().trim();

        writeMessage(message, username);

        sendText.setText("");
        sendText.requestFocus();
    }

    public void writeMessage(String message, String username) throws IOException {
        if (Objects.equals(message, "logout")) {
            leaveServer(username);
        } else if (Objects.equals(message, "userboard")) {
            getUserBoard();
        } else {
            buildAndSendMessage(message);
        }
    }

    public void joinServer(String request) throws IOException {
         this.toServer.write(joinProtocol(request.trim()));
         this.toServer.flush();
    }

    public void leaveServer(String request) throws IOException {
        this.toServer.write(leaveProtocol(request));
        this.toServer.flush();
    }

    public void getUserBoard() throws IOException {
        this.toServer.write(userBoardProtocol());
        this.toServer.flush();
    }

    public void buildAndSendMessage(String message) throws IOException {
        Pattern pattern = Pattern.compile("@\\S+");
        Matcher matcher = pattern.matcher(message);

        String formattedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        StringBuilder headerList = new StringBuilder();
        while (matcher.find()) {
            headerList.append(matcher.group().trim()).append(" ");
        }

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("sender", username);
        requestMap.put("header", headerList);
        requestMap.put("timestamp", formattedTime);
        requestMap.put("message", message.replaceAll("(^@\\w+\\s+)+", "").trim());

        displayMessage(username + " : " + message.replaceAll("(^@\\w+\\s+)+", "").trim()  + " (" + formattedTime + ")");

        ObjectMapper mapper = new ObjectMapper();

        String request = mapper.writeValueAsString(requestMap);
        System.out.println("JSON Request: " + request);

        toServer.write("SEND " + request + "\n");
        toServer.flush();
    }

    /**
     * This method responds to action events .... i.e. button clicks
     * and fulfills the contract of the ActionListener interface.
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();

        if (source == sendButton) {
            try {
                displayText();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (source == exitButton)
            System.exit(0);
    }

    /**
     * This is invoked when the user presses
     * the ENTER key.
     */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                displayText();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void keyReleased(KeyEvent e) {} // Not implemented.

    public void keyTyped(KeyEvent e) {} // Not implemented.

    private static String joinProtocol(String username) {
        return "JOIN " + username + "\n";
    }

    private static String leaveProtocol(String username) {
        return "LEAVE " + username + "\n";
    }

    private static String userBoardProtocol() {
        return "USERBOARD\n";
    }

    /** You can run multiple client profiles with the following terminal commands:
     * <p>
     * mvn -Pclient-1 exec:java -Dexec.mainClass=com.networks.ChatScreen
     * <br>
     * mvn -Pclient-2 exec:java -Dexec.mainClass=com.networks.ChatScreen
     * <br>
     * mvn -Pclient-3 exec:java -Dexec.mainClass=com.networks.ChatScreen
     * </p>
     */
    public static void main(String[] args) {

        try  {
            Socket socket = new Socket(args[0], PORT);
            ChatScreen win = new ChatScreen(socket, args[1]);

            Thread ReaderThread = new Thread(new ReaderThread(socket, win, args[1]));
            ReaderThread.start();

            win.joinServer(args[1]);

        } catch (IOException ioe) {
            System.err.println("Exception: " + ioe);
        }
    }
}