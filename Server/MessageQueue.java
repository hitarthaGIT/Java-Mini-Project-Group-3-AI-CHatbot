import java.util.*;

public class MessageQueue {
    private Queue<ClientHandler> queue = new LinkedList<>();

    public synchronized boolean canType(ClientHandler client) {
        if (queue.isEmpty() || queue.peek() == client) {
            if (!queue.contains(client)) queue.add(client);
            return true;
        }
        return false;
    }

    public synchronized void markAsProcessed(ClientHandler client) {
        if (queue.peek() == client) {
            queue.poll(); // Remove the client from the front of the queue
        }
    }
}
