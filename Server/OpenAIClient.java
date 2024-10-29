import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class OpenAIClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private String apiKey;

    // List to hold the conversation history
    private List<JSONObject> conversationHistory = new LinkedList<>();
    private static final int MAX_HISTORY = 5; // Limit to last 5 messages

    public OpenAIClient() {
        // Load the API key from the .env file
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("OPENAI_API_KEY");
    }

    public String getResponse(String userMessage) {
        try {
            // Create the connection to OpenAI API
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create the JSON input for the request body
            String jsonInputString = createJsonInput(userMessage);

            // Send the request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the response
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Extract the chatbot response using JSON.org
            String botResponse = extractChatbotResponse(response.toString());

            // Store the user message and chatbot response in the history
            storeInConversationHistory(userMessage, botResponse);

            return botResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while getting a response.";
        }
    }

    // Method to create the JSON input with system prompt and conversation history
    private String createJsonInput(String userMessage) {
        JSONArray messagesArray = new JSONArray();

        // Always include the system message at the start
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a general utility chatbot developed by Group 3 for their Java MiniProject. "
                + "You can engage in casual conversations, assist with serious brainstorming, "
                + "and provide mental health support. Be funny, but not all the time and you will never use text formatting like **,// etc and not ans pointwise.");
        messagesArray.put(systemMessage);

        // Add the conversation history to the request
        for (JSONObject pastMessage : conversationHistory) {
            messagesArray.put(pastMessage);
        }

        // Add the new user message to the request
        JSONObject userMessageObject = new JSONObject();
        userMessageObject.put("role", "user");
        userMessageObject.put("content", userMessage);
        messagesArray.put(userMessageObject);

        JSONObject jsonInput = new JSONObject();
        jsonInput.put("model", "gpt-4o-mini-2024-07-18");
        jsonInput.put("messages", messagesArray);

        return jsonInput.toString();
    }

    // Method to extract the chatbot's response from the JSON
    private String extractChatbotResponse(String jsonResponse) {
        try {
            // Convert response to a JSONObject
            JSONObject responseObject = new JSONObject(jsonResponse);

            // Get the "choices" array
            JSONArray choicesArray = responseObject.getJSONArray("choices");

            // Extract the first object in the "choices" array
            JSONObject firstChoice = choicesArray.getJSONObject(0);

            // Get the "message" object and extract the "content"
            JSONObject messageObject = firstChoice.getJSONObject("message");
            return messageObject.getString("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "No response found";
        }
    }

    // Method to store both user and bot messages in conversation history
    private void storeInConversationHistory(String userMessage, String botResponse) {
        // Add user message to the history
        JSONObject userMessageObject = new JSONObject();
        userMessageObject.put("role", "user");
        userMessageObject.put("content", userMessage);
        conversationHistory.add(userMessageObject);

        // Add bot response to the history
        JSONObject botMessageObject = new JSONObject();
        botMessageObject.put("role", "assistant");
        botMessageObject.put("content", botResponse);
        conversationHistory.add(botMessageObject);

        // Limit history size
        if (conversationHistory.size() > MAX_HISTORY * 2) {  // Multiply by 2 because it's user-bot pairs
            conversationHistory.remove(0); // Remove oldest message
            conversationHistory.remove(0); // Remove oldest bot response
        }
    }
}
