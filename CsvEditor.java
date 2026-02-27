import javax.swing.*;
import java.io.*;
import java.awt.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CsvEditor {
    private static String filePath;

    public static void main(String[] args) {
        JFrame frame = new JFrame("CSV Editor");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JButton loadButton = new JButton("Load CSV");
        JButton editButton = new JButton("Edit CSV");
        JButton saveButton = new JButton("Save CSV");

        loadButton.addActionListener(e -> openFile(textArea));
        editButton.addActionListener(e -> {
            textArea.setEditable(true);
        });
        saveButton.addActionListener(e -> {
            saveCsv(textArea.getText());
            textArea.setEditable(false);
        });

        JPanel panel = new JPanel();
        panel.add(loadButton);
        panel.add(editButton);
        panel.add(saveButton);
        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private static void openFile(JTextArea textArea) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            filePath = fileToOpen.getAbsolutePath();
            loadCsv(textArea);
        }
    }

    private static void loadCsv(JTextArea textArea) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder csvData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                csvData.append(line).append("\n");
            }
            textArea.setText(csvData.toString());
            textArea.setEditable(false);
        } catch (IOException e) {
            e.printStackTrace();
            textArea.setText("Error loading CSV file.");
        }
    }

    private static void saveCsv(String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(text);
            JOptionPane.showMessageDialog(null, "CSV file saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving CSV file.");
        }
    }
}
