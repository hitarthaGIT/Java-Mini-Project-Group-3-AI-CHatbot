import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.pdfbox.Loader; // Make sure to import the Loader class
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ChatServer {
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<String, PrintWriter> userSessions = new HashMap<>();
    private OpenAIClient openAIClient;

    public ChatServer() {
        this.openAIClient = new OpenAIClient();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Chat server started...");
        ChatServer server = new ChatServer();
        ServerSocket serverSocket = new ServerSocket(12345);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket, server.openAIClient).start();
        }
    }

    public static void broadcastMessage(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private OpenAIClient openAIClient;
        private String username;

        public ClientHandler(Socket socket, OpenAIClient openAIClient) {
            this.socket = socket;
            this.openAIClient = openAIClient;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                handleLogin();

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from " + username + ": " + message);

                    if (message.startsWith("/upload ")) {
                        String filePath = message.substring(8);
                        String documentText = parseDocument(filePath);
                        String response = generateDocumentBasedResponse(username, documentText);
                        ChatServer.broadcastMessage("Responding to " + username + ": " + response);
                    } else {
                        String response = openAIClient.getResponse(message);
                        System.out.println("Generated response: " + response);
                        ChatServer.broadcastMessage("Responding " + username +": " + response);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    synchronized (clientWriters) {
                        clientWriters.remove(out);
                        if (username != null) {
                            userSessions.remove(username);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleLogin() throws IOException {
            username = in.readLine();

            synchronized (userSessions) {
                while (userSessions.containsKey(username)) {
                    out.println("Username already taken, please choose another:");
                    username = in.readLine();
                }
                userSessions.put(username, out);
            }

            ChatServer.broadcastMessage(username + " has joined the chat!");
            out.println("Welcome, " + username + "!");
        }

        // Method to parse a PDF document
        private String parseDocument(String filePath) {
            StringBuilder text = new StringBuilder();

            try (PDDocument document = Loader.loadPDF(new File(filePath))) { // Updated to use Loader.loadPDF
                if (!document.isEncrypted()) {
                    PDFTextStripper pdfStripper = new PDFTextStripper();
                    text.append(pdfStripper.getText(document));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Failed to parse the document.";
            }

            return text.toString();
        }

        // Generate a document-based response
        private String generateDocumentBasedResponse(String userId, String documentText) {
            String userMessage = "Analyze the document content.";
            String response = openAIClient.getResponse("User " + userId + " provided a document with the following content: " + documentText);
            return response;
        }
    }
}
