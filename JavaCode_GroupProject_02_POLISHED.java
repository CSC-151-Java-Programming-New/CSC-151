// Overall members who contributed to the polished project:
// Alyssa Young (Aly3673) - Contributed file I/O system and editing function.
// Bryanna Wilson (wilsonb2742) - Contributed file I/O system, list layout for the main panel, detail panel function, and CSV files with player and staff information.
// Alexander Charles (aswe344444) - Contributed a search filter for main panel.

import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class JavaCode_GroupProject_02_POLISHED {
    private static String filePath;
    private static JTable table;
    private static DefaultTableModel tableModel;
    private static JPanel cardPanel;
    private static JPanel mainPanel;
    private static JPanel editPanel;
    private static JList<String> csvList;
    private static DefaultListModel<String> listModel;
    private static JButton editButton;
    private static JTextField searchField;
    private static boolean isLoading = false;
    private static JComboBox<String> sortDropdown;
    private static java.util.List<Object[]> originalData = new java.util.ArrayList<>();
    private static java.util.List<String> originalHeaders = new java.util.ArrayList<>();

    // Written by: Alyssa Young & Bryanna Wilson – Initializes the GUI components, sets up the layout using CardLayout for screen switching, and defines the primary event listeners for searching and button actions.
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("NFL Carolina Panthers Team Roster");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardPanel = new JPanel(new CardLayout());
        mainPanel = new JPanel(new BorderLayout());
        editPanel = new JPanel(new BorderLayout());
        listModel = new DefaultListModel<>();
        csvList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(csvList);

        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                refreshListModel();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        JButton loadButton = new JButton("Load CSV");
        editButton = new JButton("Edit CSV");
        editButton.setEnabled(false);

        Color panthersBlue = new Color(0, 133, 202);
        Color silver = new Color(165, 172, 175);
        loadButton.setBackground(silver);
        editButton.setBackground(silver);

        loadButton.addActionListener(e -> openFile());
        editButton.addActionListener(e -> switchToEditPanel());

        JPanel mainButtonPanel = new JPanel();
        mainButtonPanel.add(loadButton);
        mainButtonPanel.add(editButton);
        mainPanel.add(mainButtonPanel, BorderLayout.NORTH);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        mainPanel.add(searchPanel, BorderLayout.SOUTH);        
        mainPanel.add(listScrollPane, BorderLayout.CENTER);
        sortDropdown = new JComboBox<>(new String[]{"Sort By..."});
        sortDropdown.setBackground(Color.WHITE);

        sortDropdown.addActionListener(e -> {
            String selected = (String) sortDropdown.getSelectedItem();
            if (selected != null && !isLoading) {
                sortDataByColumn(selected);
            }
        });

        searchPanel.add(new JLabel(" "));
        searchPanel.add(sortDropdown);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
        });
        searchPanel.add(clearButton);

        mainButtonPanel.setBackground(panthersBlue);
        searchPanel.setBackground(panthersBlue);
        clearButton.setBackground(silver);
        
        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));

        tableModel = new CustomTableModel();
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().addColumnModelListener(new javax.swing.event.TableColumnModelListener() {
           @Override
           public void columnMarginChanged(javax.swing.event.ChangeEvent e) {
            adjustRowHeights(table);
           }
           
           @Override public void columnAdded(javax.swing.event.TableColumnModelEvent e) {}
           @Override public void columnRemoved(javax.swing.event.TableColumnModelEvent e) {}
           @Override public void columnMoved(javax.swing.event.TableColumnModelEvent e) {}
           @Override public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) {}
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        TextAreaRenderer textRenderer = new TextAreaRenderer();
        table.setDefaultRenderer(Object.class, textRenderer);
        table.setDefaultEditor(Object.class, new TextAreaEditor());

        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (!isLoading) {
                    if (e.getType() == TableModelEvent.UPDATE) {
                        adjustRowHeights(table);
                    } else {
                        adjustRowHeights(table);
                    }
                }
            }
        });
        
        JScrollPane tablescrollPane = new JScrollPane(table);
        table.setCellSelectionEnabled(true);

        csvList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!csvList.isSelectionEmpty()) {
                    String selected = csvList.getSelectedValue();
                    showDetails(selected);
                }
            }
        });

        JButton saveButton = new JButton("Save CSV");
        JButton addRowButton = new JButton("Add Row");
        JButton addColumnButton = new JButton("Add Column");
        JButton deleteRowButton = new JButton("Delete Row");
        JButton deleteColumnButton = new JButton("Delete Column");

        saveButton.addActionListener(e -> {
            if (table.isEditing()) {
                TableCellEditor editor = table.getCellEditor();
                if (editor != null) editor.stopCellEditing();
                else table.getDefaultEditor(table.getColumnClass(table.getEditingColumn())).stopCellEditing();
            }

            saveCsv();
            refreshListModel();
            csvList.revalidate();
            csvList.repaint();
            switchToMainPanel();
        });

        addRowButton.addActionListener(e -> {
            Object[] newRowData = new Object[tableModel.getColumnCount()];
            for (int i = 0; i < newRowData.length; i++) {
                newRowData[i] = "";
            }
            tableModel.addRow(newRowData);
            adjustRowHeights(table);
        });
        
        addColumnButton.addActionListener(e -> {
            String newColumnName = JOptionPane.showInputDialog("Enter name for new column:");
            if (newColumnName != null && !newColumnName.trim().isEmpty()) {
                tableModel.addColumn(newColumnName);
                adjustColumnWidths();
                adjustRowHeights(table);
            }
        });

        deleteRowButton.addActionListener(e -> {
            int rowCount = tableModel.getRowCount();
            if (rowCount > 0) {
                tableModel.removeRow(rowCount - 1);
                adjustRowHeights(table);
            }
        });

        deleteColumnButton.addActionListener(e -> {
            int columnCount = tableModel.getColumnCount();
            if (columnCount > 0) {
                tableModel.setColumnCount(columnCount - 1);
                adjustColumnWidths();
                adjustRowHeights(table);
            }
        });

        editPanel.add(tablescrollPane, BorderLayout.CENTER);

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

    // Written by: Alyssa Young – Manages the transition to the editing interface, enabling the table and triggering the width calculation to ensure column data is readable.
    private static void switchToEditPanel() {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, "EditPanel");
        table.setEnabled(true);
        adjustColumnWidths();
    }

    // Written by: Alyssa Young – Switches the UI back to the main viewing screen and disables table editing to protect data integrity while in view-only mode.
    private static void switchToMainPanel() {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, "MainPanel");
        table.setEnabled(false);
    }

    // Written by: Bryanna Wilson & Alyssa Young – Handles the file selection process and triggers the CSV parsing; it also captures a "master copy" of headers and data to allow for the "Reset" feature.
    private static void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(null);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            isLoading = true;
            File fileToOpen = fileChooser.getSelectedFile();
            filePath = fileToOpen.getAbsolutePath();

            table.setRowSorter(null);
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            listModel.clear();

            boolean ok = loadCsv();

            if (ok) {

                tableModel.fireTableStructureChanged();
                editButton.setEnabled(true);

                SwingUtilities.invokeLater(() -> {
                    table.setAutoCreateRowSorter(true);

                    originalHeaders.clear();
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        originalHeaders.add(tableModel.getColumnName(i));
                    }

                    originalData.clear();
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                        Object[] row = new Object[tableModel.getColumnCount()];
                        for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        row[j] = tableModel.getValueAt(i, j);
                        }
                        originalData.add(row);
                    }

                    refreshListModel();
                    adjustColumnWidths();
                    adjustRowHeights(table);

                    sortDropdown.removeAllItems();
                    sortDropdown.addItem("Sort By...");
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        sortDropdown.addItem(tableModel.getColumnName(i));
                    }
                    
                    csvList.revalidate();
                    csvList.repaint();
                    table.revalidate();
                    table.repaint();

                    isLoading = false;
                    JOptionPane.showMessageDialog(null, "CSV file loaded successfully.");
                });
            } else {
                isLoading = false;
                JOptionPane.showMessageDialog(null, "Error loading file.");
            }
        }
    }

    // Written by: Alyssa Young – Performs the low-level file reading using BufferedReader to parse CSV lines into headers and row data for the TableModel.
    private static boolean loadCsv() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            listModel.clear();
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            if ((line = reader.readLine()) != null) {
                String[] headers = line.split(",");
                for (String header : headers) {
                    tableModel.addColumn(header);
                }
            }

            int colCount = tableModel.getColumnCount();
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",", colCount);

                if (rowData.length < colCount) {
                    String[] padded = new String[colCount];
                    System.arraycopy(rowData, 0, padded, 0, rowData.length);
                    for (int k = rowData.length; k < padded.length; k++) padded[k] = "";
                    rowData = padded;
                }
                listModel.addElement(rowData[0]);
                tableModel.addRow(rowData);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Written by: Alyssa Young – Implements a custom sorting algorithm that handles both alphabetical and numerical data; it also restores the original CSV state when "Sort By..." is selected.
    private static void sortDataByColumn(String columnName) {
        if (tableModel.getRowCount() <= 1) return;

        if (columnName.equals("Sort By...")) {
            isLoading = true;
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            for (String header : originalHeaders) {
                tableModel.addColumn(header);
            }
            for (Object[] row : originalData) {
                tableModel.addRow(row);
            }
            isLoading = false;
            refreshListModel();
            adjustColumnWidths();
            adjustRowHeights(table);
            table.revalidate();
            table.repaint();
            return;
        }


        int colIndex = -1;
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (tableModel.getColumnName(i).equals(columnName)) {
                colIndex = i;
                break;
            }
        }

        if (colIndex != -1) {
            final int finalCol = colIndex;
            java.util.List<Object[]> data = new java.util.ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object[] row = new Object[tableModel.getColumnCount()];
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    row[j] = tableModel.getValueAt(i, j);
                }
                data.add(row);
            }
            data.sort((a, b) -> {
                String valA = String.valueOf(a[finalCol]).trim();
                String valB = String.valueOf(b[finalCol]).trim();

                if (valA.matches("\\d+") && valB.matches("\\d+")) {
                    return Integer.compare(Integer.parseInt(valA), Integer.parseInt(valB));
                }
                return valA.compareToIgnoreCase(valB);
            });
            isLoading = true;
            tableModel.setRowCount(0);
            for (Object[] row : data) {
                tableModel.addRow(row);
            }
            isLoading = false;
            refreshListModel();
            table.revalidate();
            table.repaint();
        }
    }

    // Written by: Bryanna Wilson – Creates a modal dialog that displays all field information for a selected player, ensuring the data is presented in a clear, read-only format.
    private static void showDetails(String selectedItem) {
        String[] values = selectedItem.split(", ");
        
        if (values.length > 0) {
            String name = values[0].trim();
            
            JDialog dialog = new JDialog((Frame) null, "Details", true);
            JTextArea textArea = new JTextArea();
            Font font = new Font("Arial", Font.BOLD, 14);
            textArea.setFont(font);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            StringBuilder messageBuilder = new StringBuilder();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(name)) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        messageBuilder.append(tableModel.getColumnName(j))
                                      .append(": ")
                                      .append(tableModel.getValueAt(i, j))
                                      .append("\n");
                    }
                    break;
                }
            }

            textArea.setText(messageBuilder.toString());
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(300, 200));
            dialog.add(scrollPane, BorderLayout.CENTER);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> dialog.dispose());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.getRootPane().setDefaultButton(okButton);

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Error reading details.");
        }
    }

    // Written by: Alyssa Young – Persists changes back to the physical CSV file by iterating through the TableModel and writing comma-separated values to the file system.
    private static void saveCsv() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.write(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) writer.write(",");
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

    // Written by: Alyssa Young & Alexander Charles – Synchronizes the JList on the main screen with the current data in the TableModel, applying search filters in real-time as the user types.
    private static void refreshListModel() {
        listModel.clear();
        String filter = (searchField != null) ? searchField.getText().trim().toLowerCase() : "";
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean matchFound = false;

            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                String cellValue = String.valueOf(tableModel.getValueAt(i, j)).toLowerCase();
                if (cellValue.contains(filter)) {
                    matchFound = true;
                    break;
                }
            }

            if (filter.isEmpty() || matchFound) {
                StringBuilder rowDisplay = new StringBuilder();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    rowDisplay.append(tableModel.getValueAt(i, j));
                    if (j < tableModel.getColumnCount() - 1) {
                        rowDisplay.append(", ");
                    }
                }
                listModel.addElement(rowDisplay.toString());
            }
        }
        csvList.setModel(listModel);
    }

    // Written by: Alyssa Young – Dynamically calculates the preferred width of each table column by measuring the rendered size of the text within the cells.
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

    // Written by: Alyssa Young – Adjusts the height of each row to accommodate multi-line text, ensuring that all data is visible even when cells contain wrapped content.
    private static void adjustRowHeights(JTable table) {
        if (table.getRowCount() == 0 || table.getColumnCount() == 0) return;
        
        for (int row = 0; row < table.getRowCount(); row++) {
            int maxHeight = table.getRowHeight();

            for (int column = 0; column < table.getColumnCount(); column++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                
                int colWidth = table.getColumnModel().getColumn(column).getWidth();
                comp.setSize(new Dimension(colWidth, comp.getPreferredSize().height));
                
                int prefHeight = comp.getPreferredSize().height;
                maxHeight = Math.max(maxHeight, prefHeight);
            }
            if (table.getRowHeight(row) != maxHeight + 10) {
                table.setRowHeight(row, maxHeight + 10);
            }
        }
    }

    // Written by: Alyssa Young – A custom TableCellRenderer that uses a JTextArea to support word-wrapping and dynamic text rendering within JTable cells.
    static class TextAreaRenderer extends JTextArea implements TableCellRenderer {
        public TextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());

            int colWidth = table.getColumnModel().getColumn(column).getWidth();
            this.setSize(new Dimension(colWidth, getPreferredSize().height));

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            setFont(table.getFont());
            return this;
        }
    }

    // Written by: Alyssa Young – A custom TableCellEditor that provides a scrollable JTextArea for editing, allowing users to input large amounts of text into a single table cell.
    static class TextAreaEditor extends AbstractCellEditor implements TableCellEditor {
        private final JScrollPane scrollPane;
        private final JTextArea textArea;

        public TextAreaEditor() {
            textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(null);
        }

        @Override
        public Object getCellEditorValue() {
            return textArea.getText();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            textArea.setText(value == null ? "" : value.toString());
            textArea.setFont(table.getFont());
            int colWidth = table.getColumnModel().getColumn(column).getWidth();
            scrollPane.setPreferredSize(new Dimension(colWidth, 100));
            return scrollPane;
        }
    }

    // Written by: Alyssa Young – A specialized version of DefaultTableModel that ensures empty columns are ignored and null values are handled gracefully during table updates.
    static class CustomTableModel extends DefaultTableModel {
        @Override
        public void addColumn(Object columnName) {
            if (columnName != null && !columnName.toString().trim().isEmpty()) {
                super.addColumn(columnName);
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            Object val = super.getValueAt(row, column);
            return (val == null) ? "" : val;
        }
    }
}
