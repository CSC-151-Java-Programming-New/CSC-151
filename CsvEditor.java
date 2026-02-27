import javax.swing.*;
import java.io.*;
import java.awt.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class CsvEditor {
    private static String filePath;
    private static JTable table;
    private static DefaultTableModel tableModel;
    private static JPanel cardPanel;
    private static JPanel mainPanel;
    private static JPanel editPanel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("CSV Editor");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardPanel = new JPanel(new CardLayout());
        mainPanel = new JPanel();
        editPanel = new JPanel();

        // Main panel.
        JButton loadButton = new JButton("Load CSV");
        JButton editButton = new JButton("Edit CSV");

        loadButton.addActionListener(e -> openFile());
        editButton.addActionListener(e -> switchToEditPanel());

        mainPanel.add(loadButton);
        mainPanel.add(editButton);

        // Edit panel.
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(table);
        JButton saveButton = new JButton("Save CSV");
        JButton addRowButton = new JButton("Add Row");
        JButton addColumnButton = new JButton("Add Column");

        saveButton.addActionListener(e -> {
            saveCsv();
            switchToMainPanel();
        });

        addRowButton.addActionListener(e -> tableModel.addRow(new Object[]{}));
        addColumnButton.addActionListener(e -> tableModel.addColumn("New Column"));

        editPanel.setLayout(new BorderLayout());
        editPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addRowButton);
        buttonPanel.add(addColumnButton);
        buttonPanel.add(saveButton);

        editPanel.add(buttonPanel, BorderLayout.SOUTH);

        cardPanel.add(mainPanel, "MainPanel");
        cardPanel.add(editPanel, "EditPanel");

        frame.add(cardPanel);
        frame.setVisible(true);
    }

    private static void switchToEditPanel() {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, "EditPanel");
        table.setEnabled(true);
        adjustColumnWidths();
    }

    private static void switchToMainPanel() {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, "MainPanel");
        table.setEnabled(false);
        adjustColumnWidths();
    }

    private static void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            filePath = fileToOpen.getAbsolutePath();
            loadCsv();
        }
    }

    private static void loadCsv() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            tableModel.setRowCount(0);

            if ((line = reader.readLine()) != null) {
                String[] headers = line.split(",");

                if (tableModel.getColumnCount() == 0) {
                    for (String header : headers) {
                        tableModel.addColumn(header);
                    }
                }

                do {
                    String[] rowData = line.split(",");
                    tableModel.addRow(rowData);
                } while ((line = reader.readLine()) != null);
            }
            adjustColumnWidths();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading CSV file.");
        }
    }

    private static void saveCsv() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.write(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() -1) writer.write(",");
            }
            writer.newLine();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write(String.valueOf(tableModel.getValueAt(i, j)));
                    if (j < tableModel.getColumnCount() - 1) writer.write(",");
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(null, "CSV file saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving CSV file.");
        }
    }

    private static void adjustColumnWidths() {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            int width = 0;
            for (int j = 0; j < tableModel.getRowCount(); j++) {
                width = Math.max(width, table.getCellRenderer(j, i).getTableCellRendererComponent(table, 
                    tableModel.getValueAt(j, i), false, false, j, i).getPreferredSize().width);
            }
            table.getColumnModel().getColumn(i).setPreferredWidth(width + 10);
        }
    }
}
