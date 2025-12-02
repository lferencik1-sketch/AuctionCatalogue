import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.List;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;

public class LotUI extends JFrame {
    private JLabel[] imageLabels = new JLabel[3];
    private JLabel statusLabel;
    private JButton nextLotButton, prevLotButton, manualLotButton, backToSortingButton, generateDocButton;

    private Map<Integer, List<File>> lotMap; // Lot number → images in that lot
    private int currentLotNumber;

    public LotUI(Map<File, Integer> lotAssignments) {
        setTitle("Lot Viewer - Auction Cataloguing Assistant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Convert File→Lot map to Lot→File list
        lotMap = new TreeMap<>();
        for (Map.Entry<File, Integer> entry : lotAssignments.entrySet()) {
            int lotNum = entry.getValue();
            lotMap.computeIfAbsent(lotNum, k -> new ArrayList<>()).add(entry.getKey());
        }

        // Image display area (3 images max per lot)
        JPanel imagePanel = new JPanel(new GridLayout(1, 3, 10, 10));
        for (int i = 0; i < 3; i++) {
            imageLabels[i] = new JLabel("", SwingConstants.CENTER);
            imageLabels[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            imagePanel.add(imageLabels[i]);
        }

        // Status label
        statusLabel = new JLabel("Lot Viewer", SwingConstants.CENTER);

        // Buttons
        nextLotButton = new JButton("Next Lot");
        prevLotButton = new JButton("Previous Lot");
        manualLotButton = new JButton("Go to Lot...");
        backToSortingButton = new JButton("Back to Sorting UI");
        generateDocButton = new JButton("Generate Document");

        nextLotButton.addActionListener(e -> showNextLot());
        prevLotButton.addActionListener(e -> showPreviousLot());
        manualLotButton.addActionListener(e -> goToManualLot());
        backToSortingButton.addActionListener(e -> backToSortingUI());
        generateDocButton.addActionListener(e -> {
            try {
                generateLotDocument();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating document: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(prevLotButton);
        buttonPanel.add(nextLotButton);
        buttonPanel.add(manualLotButton);
        buttonPanel.add(backToSortingButton);
        buttonPanel.add(generateDocButton);

        add(statusLabel, BorderLayout.NORTH);
        add(imagePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Start on the first lot
        if (!lotMap.isEmpty()) {
            currentLotNumber = lotMap.keySet().iterator().next();
            showLot(currentLotNumber);
        } else {
            statusLabel.setText("No lots to display.");
        }
    }

    private void showLot(int lotNumber) {
        if (!lotMap.containsKey(lotNumber)) {
            JOptionPane.showMessageDialog(this, "Lot " + lotNumber + " not found.");
            return;
        }

        List<File> images = lotMap.get(lotNumber);
        statusLabel.setText("Viewing Lot " + lotNumber + " (" + images.size() + " image(s))");

        for (JLabel label : imageLabels) {
            label.setIcon(null);
            label.setText("");
        }

        for (int i = 0; i < Math.min(images.size(), 3); i++) {
            File imgFile = images.get(i);
            ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            imageLabels[i].setIcon(new ImageIcon(scaled));
            imageLabels[i].setText(imgFile.getName());
            imageLabels[i].setHorizontalTextPosition(JLabel.CENTER);
            imageLabels[i].setVerticalTextPosition(JLabel.BOTTOM);
        }
    }

    private void showNextLot() {
        Integer next = getNextLotNumber();
        if (next != null) {
            currentLotNumber = next;
            showLot(currentLotNumber);
        } else {
            JOptionPane.showMessageDialog(this, "No more lots.");
        }
    }

    private void showPreviousLot() {
        Integer prev = getPreviousLotNumber();
        if (prev != null) {
            currentLotNumber = prev;
            showLot(currentLotNumber);
        } else {
            JOptionPane.showMessageDialog(this, "No previous lots.");
        }
    }

    private void goToManualLot() {
        String input = JOptionPane.showInputDialog(this, "Enter lot number:");
        try {
            int lotNum = Integer.parseInt(input);
            if (lotMap.containsKey(lotNum)) {
                currentLotNumber = lotNum;
                showLot(currentLotNumber);
            } else {
                JOptionPane.showMessageDialog(this, "Lot " + lotNum + " not found.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid lot number.");
        }
    }

    private Integer getNextLotNumber() {
        List<Integer> keys = new ArrayList<>(lotMap.keySet());
        int index = keys.indexOf(currentLotNumber);
        if (index >= 0 && index < keys.size() - 1) return keys.get(index + 1);
        return null;
    }

    private Integer getPreviousLotNumber() {
        List<Integer> keys = new ArrayList<>(lotMap.keySet());
        int index = keys.indexOf(currentLotNumber);
        if (index > 0) return keys.get(index - 1);
        return null;
    }

    private void backToSortingUI() {
        JOptionPane.showMessageDialog(this, "Returning to Sorting UI (to be implemented).");
    }

    // --- Generates Word document with lots ---
    private void generateLotDocument() {
        try (XWPFDocument doc = new XWPFDocument()) {
            for (Map.Entry<Integer, List<File>> entry : lotMap.entrySet()) {
                int lotNum = entry.getKey();
                List<File> images = entry.getValue();

                // Lot title
                XWPFParagraph titlePara = doc.createParagraph();
                XWPFRun titleRun = titlePara.createRun();
                titleRun.setBold(true);
                titleRun.setFontSize(14);
                titleRun.setText("Lot " + lotNum);

                // Create a table with 1 row, 2 columns
                XWPFTable table = doc.createTable(1, 2);
                table.setWidth("100%");

                XWPFTableCell leftCell = table.getRow(0).getCell(0);
                XWPFTableCell rightCell = table.getRow(0).getCell(1);

                // Clear default paragraphs
                leftCell.removeParagraph(0);
                rightCell.removeParagraph(0);

                // Add all images to left cell
                for (File imgFile : images) {
                    try (FileInputStream fis = new FileInputStream(imgFile)) {
                        XWPFParagraph imgPara = leftCell.addParagraph();
                        imgPara.setAlignment(ParagraphAlignment.CENTER);
                        imgPara.createRun().addPicture(fis,
                                XWPFDocument.PICTURE_TYPE_JPEG,
                                imgFile.getName(),
                                Units.toEMU(150), Units.toEMU(150));
                    }
                }

                // Add Reserve/Estimation to right cell
                XWPFParagraph infoPara = rightCell.addParagraph();
                XWPFRun infoRun = infoPara.createRun();
                infoRun.setText("Reserve price:\nEstimation:");
                infoRun.setFontSize(12);
            }

            // Save in the same location as before
            String outputPath = "AuctionLots.docx";
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                doc.write(out);
            }

            JOptionPane.showMessageDialog(this, "Document saved as " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating document:\n" + e.getMessage());
        }
    }



    // --- For standalone testing ---
    public static void main(String[] args) {
        Map<File, Integer> fakeAssignments = new HashMap<>();
        fakeAssignments.put(new File("example1.jpg"), 1);
        fakeAssignments.put(new File("example2.jpg"), 1);
        fakeAssignments.put(new File("example3.jpg"), 2);
        fakeAssignments.put(new File("example4.jpg"), 2);
        fakeAssignments.put(new File("example5.jpg"), 3);

        SwingUtilities.invokeLater(() -> new LotUI(fakeAssignments).setVisible(true));
    }
}


