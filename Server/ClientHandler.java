import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageQueue messageQueue;

    public ClientHandler(Socket socket, MessageQueue queue) {
        this.clientSocket = socket;
        this.messageQueue = queue;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Welcome to the chat! Please wait for your turn to type.");

            while (true) {
                String clientMessage = in.readLine();
                if (clientMessage != null && messageQueue.canType(this)) {
                    // Process the message (call the OpenAI API or some logic)
                    ChatServer.broadcastMessage("User typing: " + clientMessage);
                    messageQueue.markAsProcessed(this); // Free up for the next user in the queue
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
