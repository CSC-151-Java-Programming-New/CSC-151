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
        mainPanel = new JPanel(new BorderLayout());
        editPanel = new JPanel(new BorderLayout());

        // Main panel.
        JButton loadButton = new JButton("Load CSV");
        JButton editButton = new JButton("Edit CSV");

        loadButton.addActionListener(e -> openFile());
        editButton.addActionListener(e -> switchToEditPanel());

        JPanel mainButtonPanel = new JPanel();
        mainButtonPanel.add(loadButton);
        mainButtonPanel.add(editButton);
        mainPanel.add(mainButtonPanel, BorderLayout.NORTH);

        // Edit panel.
        tableModel = new CustomTableModel();
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton saveButton = new JButton("Save CSV");
        JButton addRowButton = new JButton("Add Row");
        JButton addColumnButton = new JButton("Add Column");
        JButton deleteRowButton = new JButton("Delete Row");
        JButton deleteColumnButton = new JButton("Delete Column");

        saveButton.addActionListener(e -> {
            saveCsv();
            switchToMainPanel();
        });

        addRowButton.addActionListener(e -> {
            Object[] newRowData = new Object[tableModel.getColumnCount()];
            for (int i = 0; i < newRowData.length; i++) {
                newRowData[i] = "";
            }
            tableModel.addRow(newRowData);
        });
        
        addColumnButton.addActionListener(e -> {
            String newColumnName = JOptionPane.showInputDialog("Enter name for new column:");
            if (newColumnName != null && !newColumnName.trim().isEmpty()) {
                tableModel.addColumn(newColumnName);
            }
        });

        deleteRowButton.addActionListener(e -> {
            int rowCount = tableModel.getRowCount();
            if (rowCount > 0) {
                tableModel.removeRow(rowCount - 1);
            }
        });

        deleteColumnButton.addActionListener(e -> {
            int columnCount = tableModel.getColumnCount();
            if (columnCount > 0) {
                tableModel.setColumnCount(columnCount - 1);
            }
        });

        editPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addRowButton);
        buttonPanel.add(deleteRowButton);
        buttonPanel.add(addColumnButton);
        buttonPanel.add(deleteColumnButton);
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
                for (String header : headers) {
                    tableModel.addColumn(header);
                }

                while ((line = reader.readLine()) != null) {
                    String[] rowData = line.split(",");
                    tableModel.addRow(rowData);
                }
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
                width = Math.max(width, table.getCellRenderer(j, i)
                    .getTableCellRendererComponent(table, tableModel.getValueAt(j, i), false, false, j, i)
                    .getPreferredSize().width);
            }
            table.getColumnModel().getColumn(i).setPreferredWidth(width + 10);
        }
    }

    static class CustomTableModel extends DefaultTableModel {
        @Override
        public void addColumn(Object columnName) {
            if (columnName != null && !columnName.toString().trim().isEmpty()) {
                super.addColumn(columnName);
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            Object value = super.getValueAt(row, column);
            return value == null ? "" : value;
        }
    }
}
