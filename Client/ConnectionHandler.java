import java.io.*;
import java.net.*;

import javax.swing.JScrollBar;

public class ConnectionHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ChatbotGUI gui;

    public ConnectionHandler(String serverAddress, int port, ChatbotGUI gui) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.gui = gui;
    }

    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                gui.displayMessage(message);  // Update the GUI with new messages
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 

    public void sendMessage(String message) {
        out.println(message);  // Send message to the server
    }
}
