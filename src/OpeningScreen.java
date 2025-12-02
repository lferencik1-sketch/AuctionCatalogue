import javax.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class OpeningScreen extends JFrame {

    private JLabel instructionLabel;
    private JButton selectFolderButton;

    public OpeningScreen() {
        // Window title and setup
        setTitle("Auction Cataloguing Assistant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 300);
        setLocationRelativeTo(null); // Center the window
        setLayout(new BorderLayout(20, 20));

        // --- Main instruction text ---
        instructionLabel = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<h2>Welcome to Auction Cataloguing Assistant</h2>"
                        + "Choose a folder containing all the images you want to catologue."
                        + "</div></html>",
                SwingConstants.CENTER
        );

        //button setup
        selectFolderButton = new JButton("Select Folder");
        selectFolderButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        selectFolderButton.setFocusPainted(false);
        selectFolderButton.setPreferredSize(new Dimension(200, 50));

        // Action listener for folder
        selectFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int result = chooser.showOpenDialog(OpeningScreen.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFolder = chooser.getSelectedFile();
                    new ImageSortingUI(selectedFolder).setVisible(true);
                    dispose(); // closes OpeningScreen
                }
            }
        });

        // layout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectFolderButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(instructionLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(selectFolderButton);

        add(centerPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new OpeningScreen().setVisible(true);
        });
    }
}