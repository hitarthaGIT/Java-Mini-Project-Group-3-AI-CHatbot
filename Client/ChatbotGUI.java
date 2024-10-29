import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.net.Socket;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
// Custom panel for rounded corners
class RoundedPanel extends JPanel {
    private int cornerRadius;

    public RoundedPanel(int radius) {
        super();
        this.cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
    }
}

// Custom rounded button
class RoundedButton extends JButton {
    private int cornerRadius;

    public RoundedButton(String text, int radius) {
        super(text);
        this.cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Draw the text in the center of the button
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(getText());
        int textHeight = fm.getAscent();
        g2.setColor(getForeground());
        g2.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 3);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
    }
}

// Custom rounded text field
class RoundedTextField extends JTextField {
    private int cornerRadius;

    public RoundedTextField(int radius) {
        super();
        this.cornerRadius = radius;
        setOpaque(false);
        setFont(new Font("Arial", Font.PLAIN, 20)); // Set larger font for the input text
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Text
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    public void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2.dispose();
    }
}

public class ChatbotGUI extends JFrame {
    private String username;
    private String username2;
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private RoundedTextField inputField; 
    private RoundedButton sendButton, imageButton; 
    private BufferedImage backgroundImage; 
    private JPanel backgroundPanel; 
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private RoundedButton uploadPDFButton; 

    public ChatbotGUI() {
        // Establish a connection to the server
        try {
            socket = new Socket("localhost", 12345);  
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            listenerThread = new Thread(new ServerListener());
            listenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up the frame
        setTitle("AI Chatbot - Group 3");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set up title
        JLabel titleLabel = new JLabel("Welcome to Group 3's AI Chatbot", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Set up background panel (holds all chat and input components)
        backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setBackground(Color.WHITE); // Default background

        // Set up chat panel (holds all messages)
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setOpaque(false);

        // Set up scroll pane for chat messages
        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        // Set up input field and rounded send button
        inputField = new RoundedTextField(20); 
        inputField.setPreferredSize(new Dimension(400, 70)); 
        sendButton = new RoundedButton("SEND", 20); 
        sendButton.setFont(new Font("Arial", Font.BOLD, 20));
        sendButton.setBackground(Color.GREEN); 
        sendButton.setForeground(Color.BLUE);

        // Add the image select button
        imageButton = new RoundedButton("Select Background", 20);
        imageButton.setFont(new Font("Arial", Font.BOLD, 16));
        imageButton.setBackground(Color.CYAN);
        imageButton.setForeground(Color.BLACK);

        // Create a panel for the input field and buttons
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setOpaque(false);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        backgroundPanel.add(inputPanel, BorderLayout.SOUTH);

        // Add the image button to the top of the background panel
        backgroundPanel.add(imageButton, BorderLayout.NORTH);
        add(backgroundPanel, BorderLayout.CENTER);

          // Add the upload PDF button
          uploadPDFButton = new RoundedButton("Upload PDF", 20);
          uploadPDFButton.setFont(new Font("Arial", Font.BOLD, 16));
          uploadPDFButton.setBackground(Color.ORANGE);
          uploadPDFButton.setForeground(Color.BLACK);
  
          // Add action listener for PDF upload button
          uploadPDFButton.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                  selectAndSendPDFDocument();
              }
          });
  
          // Add the PDF button to the input panel
          inputPanel.add(uploadPDFButton, BorderLayout.WEST);

        // Add action listeners to the buttons
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAndSetBackgroundImage();
            }
        });

        // Allow sending the message via "Enter" key
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        setVisible(true);
        
        
    }

    private void selectAndSendPDFDocument() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (PDDocument document = Loader.loadPDF(selectedFile)) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String documentText = pdfStripper.getText(document);
    
                // Send entire document text to the backend server
                out.println("DOCUMENT:" + documentText); // Ensure this is acceptable in length
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    

    public ChatbotGUI(String username) {
        this(); // Call the default constructor to set up the GUI
        this.username = username; // Store the username
        out.println(username);
        username2=username;
        // Optionally display the username somewhere in the GUI
        JLabel welcomeLabel = new JLabel("Logged in as: " + username);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(welcomeLabel, BorderLayout.SOUTH); // Add it to the bottom of the frame
    }
    // Method to select an image and set it as the background with a blur effect
    private void selectAndSetBackgroundImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage originalImage = ImageIO.read(selectedFile);
                backgroundImage = blurImage(originalImage); // Apply the blur effect
                backgroundPanel.repaint(); // Repaint the panel with the new background
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Method to apply a blur effect with a 20x20 kernel
    private BufferedImage blurImage(BufferedImage image) {
        float[] matrix = new float[400];  
        for (int i = 0; i < 400; i++) {
            matrix[i] = 1 / 400f;  // Uniform blur
        }

        BufferedImageOp op = new ConvolveOp(new Kernel(20, 20, matrix), ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }

    // Method to create rounded message panels for the chat
    private JPanel createMessageBubble(String message, Color bgColor, Color textColor, boolean isUser) {
        RoundedPanel messagePanel = new RoundedPanel(20);
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setBackground(bgColor);
        messagePanel.setBorder(new EmptyBorder(1, 15, 1, 15));
        messagePanel.setMaximumSize(new Dimension(560, 0)); 

        JLabel messageLabel = new JLabel("<html><p style=\"width: 280px\">" + message.replace("\n", "<br>") + "</p></html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        messageLabel.setForeground(textColor);

        messagePanel.add(messageLabel, BorderLayout.CENTER);

        JPanel alignmentPanel = new JPanel(new BorderLayout());
        alignmentPanel.setOpaque(false);
        alignmentPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); 

        if (isUser) {
            alignmentPanel.add(messagePanel, BorderLayout.EAST);
        } else {
            alignmentPanel.add(messagePanel, BorderLayout.WEST);
        }

        return alignmentPanel;
    }

    private void sendMessage() {
        String userInput = inputField.getText();
        if (userInput.trim().isEmpty()) {
            return;  
        }

        chatPanel.add(createMessageBubble(userInput, new Color(182, 190, 221), Color.BLACK, true));
        chatPanel.revalidate();  
        inputField.setText("");

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());

        out.println(userInput);
    }

    // Thread to listen for server responses
    private class ServerListener implements Runnable {
        private JLabel typingLabel;
    
        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    // Check if the response starts with "Responding"
                    if (response.startsWith("Responding")) {
                        // Split the response to get username and message content
                        String[] parts = response.split(":", 2); // Split only at the first colon
                        if (parts.length < 2) {
                            continue; // Skip if the format is not as expected
                        }
                        
                        String userInfo = parts[0].trim();  // "Responding username"
                        String messageContent = parts[1].trim();  // Actual message content
                        
                        // Extract the username by removing "Responding" and trimming any extra space
                        String username = userInfo.replace("Responding", "").trim();
        
                        showTypingAnimation("Responding to "+username); // Show typing animation with the username
                        
                        Thread.sleep(1000);  // Simulate delay
                        final String serverMessage = messageContent;
                        SwingUtilities.invokeLater(() -> {
                             //  typing animation stays
                            
                            chatPanel.add(createMessageBubble(serverMessage, new Color(173, 255, 47), Color.BLACK, false));
                            chatPanel.revalidate();
                            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
                        });
                    } else {
                        // If response doesn't start with "Responding", display typing animation
                        final String directMessage = response;
                        SwingUtilities.invokeLater(() -> {
                            hideTypingAnimation();
                            chatPanel.add(createMessageBubble(directMessage, new Color(173, 255, 47), Color.BLACK, false));
                            chatPanel.revalidate();
                            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
                        });
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();  
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        
    
        // Method to show a typing animation in the chat panel
       // Method to show a typing animation with username in the chat panel
private void showTypingAnimation(String msg) {
    SwingUtilities.invokeLater(() -> {
        typingLabel = new JLabel(msg); // Display username in typing animation
        typingLabel.setFont(new Font("Arial", Font.ITALIC, 20));
        typingLabel.setForeground(Color.GRAY);
        JPanel typingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typingPanel.setOpaque(false);
        typingPanel.add(typingLabel);
        chatPanel.add(typingPanel);
        chatPanel.revalidate();
        
        // Add a timer for dynamic typing dots effect
        Timer timer = new Timer(500, new ActionListener() {
            int dotCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                dotCount = (dotCount + 1) % 4; // Loop through 0 to 3 dots
                String dots = new String(new char[dotCount]).replace("\0", ".");
                typingLabel.setText(msg + dots); // Update text with dots
            }
        });
        timer.setRepeats(true);
        timer.start();
    });
}

          // Method to remove typing animation from the chat panel
          private void hideTypingAnimation() {
            if (typingLabel != null && typingLabel.getParent() != null) {
                typingLabel.getParent().remove(typingLabel); // Safely remove typingLabel if it exists
                typingLabel = null; // Reset typingLabel to avoid reusing it by mistake
                chatPanel.revalidate();
                chatPanel.repaint();
            }
        }
        
}        
             
   public void displayMessage(String message) {
    // Create a message bubble for the received message
    chatPanel.add(createMessageBubble(message, new Color(173, 255, 47), Color.BLACK, false));
    chatPanel.revalidate(); // Refresh the chat panel to show the new message
    JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
    verticalScrollBar.setValue(verticalScrollBar.getMaximum()); // Scroll to the bottom
}  
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatbotGUI::new);
    }
}
