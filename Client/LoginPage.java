import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JButton loginButton;

    public LoginPage() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240)); // Light background

        // Title label with rounded border
        JLabel promptLabel = new JLabel("Who are you?", JLabel.CENTER);
        promptLabel.setFont(new Font("Arial", Font.BOLD, 28));
        promptLabel.setForeground(new Color(54, 54, 54)); 
        promptLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(promptLabel, BorderLayout.NORTH);

        // Center panel for input field with rounded border
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(240, 240, 240));
        inputPanel.setBorder(new RoundedBorder(15)); // Rounded border with radius 15
        inputPanel.setLayout(new BorderLayout(5, 5));

        // Username field with rounded appearance
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameField.setHorizontalAlignment(JTextField.CENTER);
        usernameField.setBorder(new RoundedBorder(15, new Color(180, 180, 180)));
        inputPanel.add(usernameField, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.CENTER);

        // Button panel with rounded button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setBackground(new Color(30, 144, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(new RoundedBorder(15)); // Rounded button border
        buttonPanel.add(loginButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Login button action listener
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    dispose(); // Close login window
                    new ChatbotGUI(username); // Pass username to Chatbot GUI
                } else {
                    JOptionPane.showMessageDialog(null, "Username cannot be empty", 
                                                  "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage());
    }

    // Custom rounded border class
    static class RoundedBorder extends LineBorder {
        private int radius;

        RoundedBorder(int radius) {
            super(new Color(180, 180, 180), 1, true);
            this.radius = radius;
        }

        RoundedBorder(int radius, Color color) {
            super(color, 1, true);
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(lineColor);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
}
