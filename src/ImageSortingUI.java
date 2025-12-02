import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class ImageSortingUI extends JFrame {
    private JLabel imageLabel;
    private JLabel statusLabel;
    private JButton prevButton, nextButton, hideButton;
    private JButton assignNextLotButton, assignPrevLotButton, assignManualLotButton, switchToLotUIButton;

    private List<File> imageFiles;
    private int currentIndex = 0;
    private Map<File, Integer> lotAssignments = new HashMap<>();
    private Set<File> hiddenImages = new HashSet<>();
    private int currentLotNumber = 1; // starts with Lot 1

    public ImageSortingUI(File folder) {
        setTitle("Image Sorting - Auction Cataloguing Assistant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Load images ---
        imageFiles = loadImages(folder);

        // --- Image preview ---
        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(700, 500));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        // --- Info label ---
        statusLabel = new JLabel("", SwingConstants.CENTER);

        // --- Control buttons ---
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        hideButton = new JButton("Hide");

        assignNextLotButton = new JButton("Assign to Next Lot");
        assignPrevLotButton = new JButton("Assign to Previous Lot");
        assignManualLotButton = new JButton("Assign to Manual Lot");
        switchToLotUIButton = new JButton("Switch to Lot UI");

        // --- Actions ---
        prevButton.addActionListener(e -> showPreviousImage());
        nextButton.addActionListener(e -> showNextImage());
        hideButton.addActionListener(e -> hideCurrentImage());
        assignNextLotButton.addActionListener(e -> assignToNextLot());
        assignPrevLotButton.addActionListener(e -> assignToPreviousLot());
        assignManualLotButton.addActionListener(e -> assignToManualLot());
        switchToLotUIButton.addActionListener(e -> switchToLotUI());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(hideButton);
        buttonPanel.add(assignNextLotButton);
        buttonPanel.add(assignPrevLotButton);
        buttonPanel.add(assignManualLotButton);
        buttonPanel.add(switchToLotUIButton);

        add(imageLabel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Start with first image ---
        if (!imageFiles.isEmpty()) {
            showImage(currentIndex);
        } else {
            statusLabel.setText("No images found in the selected folder.");
        }
    }

    private List<File> loadImages(File folder) {
        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")
        );
        if (files != null) {
            Arrays.sort(files);
            return new ArrayList<>(Arrays.asList(files));
        }
        return new ArrayList<>();
    }

    private void showImage(int index) {
        if (imageFiles.isEmpty()) return;

        File imgFile = imageFiles.get(index);
        ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
        int w = imageLabel.getWidth();
        int h = imageLabel.getHeight();

        // Fallback if label isn't visible yet
        if (w <= 0 || h <= 0) {
            w = 700;
            h = 500;
        }

        Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));

        String lotInfo = lotAssignments.containsKey(imgFile)
                ? "Lot " + lotAssignments.get(imgFile)
                : "Unassigned";

        String hidden = hiddenImages.contains(imgFile) ? " (Hidden)" : "";

        statusLabel.setText(String.format(
                "Image %d of %d: %s | %s%s",
                index + 1, imageFiles.size(), imgFile.getName(), lotInfo, hidden
        ));
    }

    private void showNextImage() {
        if (imageFiles.isEmpty()) return;
        currentIndex = (currentIndex + 1) % imageFiles.size();
        showImage(currentIndex);
    }

    private void showPreviousImage() {
        if (imageFiles.isEmpty()) return;
        currentIndex = (currentIndex - 1 + imageFiles.size()) % imageFiles.size();
        showImage(currentIndex);
    }

    private void hideCurrentImage() {
        File current = imageFiles.get(currentIndex);
        hiddenImages.add(current);
        JOptionPane.showMessageDialog(this, "Image hidden: " + current.getName());
        showImage(currentIndex);
    }

    private void assignToNextLot() {
        File current = imageFiles.get(currentIndex);
        lotAssignments.put(current, currentLotNumber);
        JOptionPane.showMessageDialog(this,
                current.getName() + " assigned to Lot " + currentLotNumber);
        currentLotNumber++;
        showNextImage(); // move to next image automatically
    }

    private void assignToPreviousLot() {
        if (currentLotNumber <= 1) {
            JOptionPane.showMessageDialog(this, "No previous lot available.");
            return;
        }
        File current = imageFiles.get(currentIndex);
        lotAssignments.put(current, currentLotNumber - 1);
        JOptionPane.showMessageDialog(this,
                current.getName() + " assigned to Lot " + (currentLotNumber - 1));
        showNextImage(); // move to next image automatically
    }

    private void assignToManualLot() {
        File current = imageFiles.get(currentIndex);
        String input = JOptionPane.showInputDialog(this, "Enter lot number:");
        try {
            int lotNum = Integer.parseInt(input);
            lotAssignments.put(current, lotNum);
            JOptionPane.showMessageDialog(this,
                    current.getName() + " manually assigned to Lot " + lotNum);
            showNextImage(); // move to next image automatically
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid lot number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchToLotUI() {
        if (lotAssignments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No images have been assigned to lots yet.");
            return;
        }
        new LotUI(lotAssignments).setVisible(true);
        dispose(); // close the current ImageSortingUI
    }

    // --- For standalone testing ---
    public static void main(String[] args) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File folder = chooser.getSelectedFile();
            SwingUtilities.invokeLater(() -> new ImageSortingUI(folder).setVisible(true));
        }
    }
}
