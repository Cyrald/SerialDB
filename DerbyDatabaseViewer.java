import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.SerializationUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class DerbyDatabaseViewer extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private static final float WINDOW_WIDTH_PERCENT = 0.7f;
    private static final float WINDOW_HEIGHT_PERCENT = 0.7f;
    private static final int TABLES_LIST_WIDTH = 250;
    
    private JTextField dbPathField;
    private JButton browseButton;
    private JButton openButton;
    private JList<String> tablesList;
    private DefaultListModel<String> tablesListModel;
    private JTextField searchTableField;
    private JTextField searchDataField;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JCheckBox deserializeObjectsCheckbox;
    private JButton copyButton;
    private JButton createTableButton;
    private JButton deleteTableButton;
    private JButton addRowButton;
    private JButton editRowButton;
    private JButton deleteRowButton;
    private JSplitPane mainSplitPane;
    private JPanel headerViewport;
    private Map<Integer, JTextField> columnFilters = new HashMap<>();
    
    private Connection connection;
    private Database database;
    private ArrayList<String> currentTableColumns = new ArrayList<>();
    private ArrayList<String> allTables = new ArrayList<>();
    private ObjectMapper objectMapper;
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            DerbyDatabaseViewer viewer = new DerbyDatabaseViewer();
            viewer.setVisible(true);
        });
    }
    
    public DerbyDatabaseViewer() {
        super("Просмотрщик баз данных Derby");
        
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        setupUI();
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeConnection();
                System.exit(0);
            }
        });
    }
    
    private void setupUI() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)(screenSize.width * WINDOW_WIDTH_PERCENT);
        int height = (int)(screenSize.height * WINDOW_HEIGHT_PERCENT);
        
        setSize(width, height);
        setLocationRelativeTo(null);
        
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel pathLabel = new JLabel("Путь к базе данных:");
        dbPathField = new JTextField();
        browseButton = new JButton("Обзор...");
        openButton = new JButton("Открыть");
        
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonsPanel.add(browseButton);
        buttonsPanel.add(openButton);
        
        topPanel.add(pathLabel, BorderLayout.WEST);
        topPanel.add(dbPathField, BorderLayout.CENTER);
        topPanel.add(buttonsPanel, BorderLayout.EAST);
        
        JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel searchTablesLabel = new JLabel("Поиск таблиц:");
        searchTableField = new JTextField();
        Dimension searchTablesSize = new Dimension(100, searchTableField.getPreferredSize().height);
        searchTableField.setPreferredSize(searchTablesSize);
        
        JPanel searchTablesPanel = new JPanel(new BorderLayout(5, 0));
        searchTablesPanel.add(searchTablesLabel, BorderLayout.NORTH);
        searchTablesPanel.add(searchTableField, BorderLayout.CENTER);
        
        tablesListModel = new DefaultListModel<>();
        tablesList = new JList<>(tablesListModel);
        tablesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tablesScrollPane = new JScrollPane(tablesList);
        
        JPanel tableButtonsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        createTableButton = new JButton("Создать");
        deleteTableButton = new JButton("Удалить");
        
        tableButtonsPanel.add(createTableButton);
        tableButtonsPanel.add(deleteTableButton);
        
        leftPanel.add(searchTablesPanel, BorderLayout.NORTH);
        leftPanel.add(tablesScrollPane, BorderLayout.CENTER);
        leftPanel.add(tableButtonsPanel, BorderLayout.SOUTH);
        
        JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel controlsPanel = new JPanel(new BorderLayout(5, 0));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        searchDataField = new JTextField();
        searchDataField.setEnabled(false);
        Dimension searchDataSize = new Dimension(100, searchDataField.getPreferredSize().height);
        searchDataField.setPreferredSize(searchDataSize);
        
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        deserializeObjectsCheckbox = new JCheckBox("Декодировать данные");
        deserializeObjectsCheckbox.setSelected(true);
        optionsPanel.add(deserializeObjectsCheckbox);
        
        JPanel searchAndOptionsPanel = new JPanel(new BorderLayout(5, 0));
        searchAndOptionsPanel.add(searchDataField, BorderLayout.CENTER);
        searchAndOptionsPanel.add(optionsPanel, BorderLayout.EAST);
        
        controlsPanel.add(searchAndOptionsPanel, BorderLayout.CENTER);
        
        tableModel = new DefaultTableModel() {
			private static final long serialVersionUID = -5909194860903493399L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dataTable = new JTable(tableModel);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        headerViewport = new JPanel(new BorderLayout());
        
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        tableScrollPane.setColumnHeaderView(headerViewport);
        
        JPanel dataButtonsPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        addRowButton = new JButton("Добавить");
        editRowButton = new JButton("Редактировать");
        deleteRowButton = new JButton("Удалить");
        copyButton = new JButton("Копировать");
        copyButton.setEnabled(false);
        
        dataButtonsPanel.add(addRowButton);
        dataButtonsPanel.add(editRowButton);
        dataButtonsPanel.add(deleteRowButton);
        dataButtonsPanel.add(copyButton);
        
        rightPanel.add(controlsPanel, BorderLayout.NORTH);
        rightPanel.add(tableScrollPane, BorderLayout.CENTER);
        rightPanel.add(dataButtonsPanel, BorderLayout.SOUTH);
        
        statusLabel = new JLabel("Готов к работе");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplitPane.setDividerLocation(TABLES_LIST_WIDTH);
        mainSplitPane.setOneTouchExpandable(true);
        
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        setupEventHandlers();
    }
    
    private void setupTableHeader() {
        columnFilters.clear();
        headerViewport.removeAll();
        
        if (tableModel.getColumnCount() == 0) {
            headerViewport.revalidate();
            headerViewport.repaint();
            return;
        }
        
        JTableHeader header = dataTable.getTableHeader();
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(header, BorderLayout.NORTH);
        
        headerViewport.add(headerPanel, BorderLayout.CENTER);
        headerViewport.revalidate();
        headerViewport.repaint();
    }
    
    private void setupEventHandlers() {
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Выберите папку с базой данных");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                dbPathField.setText(selectedFile.getAbsolutePath());
            }
        });
        
        openButton.addActionListener(e -> openDatabase());
        
        tablesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablesList.getSelectedValue() != null) {
                loadTableData(tablesList.getSelectedValue(), null);
                deleteTableButton.setEnabled(true);
            } else {
                deleteTableButton.setEnabled(false);
            }
        });
        
        searchTableField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTables();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTables();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTables();
            }
        });
        
        searchDataField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }
            
            private void search() {
                String searchText = searchDataField.getText().trim();
                if (searchText.isEmpty()) {
                    dataTable.setRowSorter(null);
                    return;
                }
                
                if (tableModel.getColumnCount() == 0) return;
                
                TableRowSorter<TableModel> sorter = new TableRowSorter<>(dataTable.getModel());
                dataTable.setRowSorter(sorter);
                
                RowFilter<TableModel, Integer> rowFilter = new RowFilter<TableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                        Object value = entry.getValue(0);
                        if (value == null) return false;
                        
                        return value.toString().toLowerCase().contains(searchText.toLowerCase());
                    }
                };
                
                sorter.setRowFilter(rowFilter);
                
                int filteredRowCount = dataTable.getRowCount();
                statusLabel.setText("Таблица: " + tablesList.getSelectedValue() + 
                                    " | Всего записей: " + tableModel.getRowCount() + 
                                    " | Отфильтровано: " + filteredRowCount + 
                                    " | Фильтр: \"" + searchText + "\"");
            }
        });
        
        ActionListener reloadListener = e -> {
            if (tablesList.getSelectedValue() != null) {
                loadTableData(tablesList.getSelectedValue(), searchDataField.getText());
            }
        };
        
        deserializeObjectsCheckbox.addActionListener(reloadListener);
        
        dataTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = dataTable.getSelectedRow() != -1;
                copyButton.setEnabled(rowSelected && dataTable.getSelectedColumn() != -1);
                editRowButton.setEnabled(rowSelected);
                deleteRowButton.setEnabled(rowSelected);
            }
        });
        
        dataTable.getColumnModel().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                copyButton.setEnabled(dataTable.getSelectedRow() != -1 && dataTable.getSelectedColumn() != -1);
            }
        });
        
        copyButton.addActionListener(e -> {
            int viewRow = dataTable.getSelectedRow();
            int viewCol = dataTable.getSelectedColumn();
            
            if (viewRow != -1 && viewCol != -1) {
                int modelRow = viewRow;
                if (dataTable.getRowSorter() != null) {
                    modelRow = dataTable.getRowSorter().convertRowIndexToModel(viewRow);
                }
                
                Object value = tableModel.getValueAt(modelRow, viewCol);
                if (value != null) {
                    try {
                        StringSelection selection = new StringSelection(value.toString());
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, null);
                        statusLabel.setText("Скопировано в буфер обмена: " + value);
                    } catch (Exception ex) {
                        statusLabel.setText("Ошибка копирования: " + ex.getMessage());
                    }
                }
            }
        });
        
        createTableButton.addActionListener(e -> {
            String tableName = JOptionPane.showInputDialog(this, "Введите имя новой таблицы:", "Создание таблицы", JOptionPane.QUESTION_MESSAGE);
            if (tableName != null && !tableName.isEmpty()) {
                createTable(tableName);
            }
        });
        
        deleteTableButton.addActionListener(e -> {
            String selectedTable = tablesList.getSelectedValue();
            if (selectedTable != null) {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Вы уверены, что хотите удалить таблицу " + selectedTable + "?",
                    "Подтверждение удаления",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteTable(selectedTable);
                }
            }
        });
        
        addRowButton.addActionListener(e -> {
            String selectedTable = tablesList.getSelectedValue();
            if (selectedTable != null) {
                addRowToTable(selectedTable);
            } else {
                JOptionPane.showMessageDialog(this, "Выберите таблицу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        editRowButton.addActionListener(e -> {
            String selectedTable = tablesList.getSelectedValue();
            int selectedRow = dataTable.getSelectedRow();
            
            if (selectedTable != null && selectedRow != -1) {
                editTableRow(selectedTable, selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Выберите строку для редактирования", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        deleteRowButton.addActionListener(e -> {
            String selectedTable = tablesList.getSelectedValue();
            int selectedRow = dataTable.getSelectedRow();
            
            if (selectedTable != null && selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Вы уверены, что хотите удалить выбранную строку?",
                    "Подтверждение удаления",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteTableRow(selectedTable, selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Выберите строку для удаления", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void filterTables() {
        String filterText = searchTableField.getText().toLowerCase();
        
        tablesListModel.clear();
        
        for (String table : allTables) {
            if (table.toLowerCase().contains(filterText)) {
                tablesListModel.addElement(table);
            }
        }
    }
    
    private void openDatabase() {
        String dbPath = dbPathField.getText().trim();
        if (dbPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Укажите путь к папке с базой данных", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        closeConnection();
        
        try {
            String url = "jdbc:derby:" + dbPath + ";create=true";
            
            connection = DriverManager.getConnection(url);
            database = new Database(dbPath);
            
            statusLabel.setText("Подключено к базе данных: " + dbPath);
            
            loadTables();
            
            searchDataField.setEnabled(true);
            createTableButton.setEnabled(true);
            addRowButton.setEnabled(true);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Ошибка подключения к базе данных: " + e.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Ошибка подключения к базе данных");
        }
    }
    
    private void loadTables() {
        tablesListModel.clear();
        allTables.clear();
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, "APP", null, new String[] {"TABLE"});
            
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tablesListModel.addElement(tableName);
                allTables.add(tableName);
            }
            
            rs.close();
            
            if (tablesListModel.getSize() > 0) {
                tablesList.setSelectedIndex(0);
            } else {
                statusLabel.setText("База данных не содержит таблиц");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Ошибка при получении списка таблиц: " + e.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTableData(String tableName, String searchText) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        currentTableColumns.clear();
        
        dataTable.setRowSorter(null);
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, "APP", tableName, null);
            
            Map<String, Integer> columnTypesMap = new HashMap<>();
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                int dataType = columns.getInt("DATA_TYPE");
                
                tableModel.addColumn(columnName);
                currentTableColumns.add(columnName);
                columnTypesMap.put(columnName, dataType);
            }
            columns.close();
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM \"" + tableName + "\"");
            
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnCount = rsMetaData.getColumnCount();
            
            while (rs.next()) {
                Object[] rowData = new Object[columnCount];
                
                for (int i = 0; i < columnCount; i++) {
                    Object value = rs.getObject(i + 1);
                    
                    if (value == null) {
                        rowData[i] = "[NULL]";
                        continue;
                    }
                    
                    if (value instanceof byte[]) {
                        byte[] bytes = (byte[]) value;
                        
                        if (deserializeObjectsCheckbox.isSelected()) {
                            try {
                                Object deserializedObject = tryToDeserializeObject(bytes);
                                if (deserializedObject != null) {
                                    rowData[i] = deserializedObject.toString();
                                    continue;
                                }
                            } catch (Exception e) {
                            }
                        }
                        
                        rowData[i] = "[BLOB: " + bytes.length + " байт]";
                    } else if (value instanceof Blob) {
                        Blob blob = (Blob) value;
                        
                        try {
                            byte[] bytes = blob.getBytes(1, (int) blob.length());
                            
                            if (deserializeObjectsCheckbox.isSelected()) {
                                try {
                                    Object deserializedObject = tryToDeserializeObject(bytes);
                                    if (deserializedObject != null) {
                                        rowData[i] = deserializedObject.toString();
                                        continue;
                                    }
                                } catch (Exception e) {
                                }
                            }
                            
                            rowData[i] = "[BLOB: " + blob.length() + " байт]";
                        } catch (SQLException e) {
                            rowData[i] = "[Ошибка чтения BLOB: " + e.getMessage() + "]";
                        }
                    } else if (value instanceof Clob) {
                        Clob clob = (Clob) value;
                        try {
                            if (clob.length() > 1000) {
                                rowData[i] = clob.getSubString(1, 1000) + 
                                            "... [Текст обрезан, полная длина: " + 
                                            clob.length() + " символов]";
                            } else {
                                rowData[i] = clob.getSubString(1, (int) clob.length());
                            }
                        } catch (SQLException e) {
                            rowData[i] = "[Ошибка чтения CLOB: " + e.getMessage() + "]";
                        }
                    } else {
                        rowData[i] = value;
                    }
                }
                
                tableModel.addRow(rowData);
            }
            
            rs.close();
            stmt.close();
            
            setupTableHeader();
            
            if (searchText != null && !searchText.trim().isEmpty()) {
                searchDataField.setText(searchText);
            } else {
                searchDataField.setText("");
            }
            
            int rowCount = tableModel.getRowCount();
            statusLabel.setText("Таблица: " + tableName + " | Записей: " + rowCount);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Ошибка при загрузке данных таблицы: " + e.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createTable(String tableName) {
        if (database != null) {
            boolean success = database.createTable(tableName);
            if (success) {
                statusLabel.setText("Таблица " + tableName + " создана успешно");
                loadTables();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Не удалось создать таблицу " + tableName, 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "База данных не открыта", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteTable(String tableName) {
        if (database != null) {
            boolean success = database.deleteTable(tableName);
            if (success) {
                statusLabel.setText("Таблица " + tableName + " удалена успешно");
                loadTables();
                
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);
                currentTableColumns.clear();
                setupTableHeader();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Не удалось удалить таблицу " + tableName, 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "База данных не открыта", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addRowToTable(String tableName) {
        try {
            String dataText = JOptionPane.showInputDialog(this, "Введите данные для добавления:", "Добавление строки", JOptionPane.QUESTION_MESSAGE);
            if (dataText != null) {
                byte[] dataBytes = dataText.getBytes(StandardCharsets.UTF_8);
                
                boolean success = database.writeData(tableName, dataBytes);
                if (success) {
                    statusLabel.setText("Данные добавлены в таблицу " + tableName);
                    loadTableData(tableName, null);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Не удалось добавить данные в таблицу " + tableName, 
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Ошибка при добавлении данных: " + ex.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editTableRow(String tableName, int rowIndex) {
        try {
            int modelRow = rowIndex;
            if (dataTable.getRowSorter() != null) {
                modelRow = dataTable.getRowSorter().convertRowIndexToModel(rowIndex);
            }
            
            int tableId = database.getIdByIndex(tableName, modelRow);
            if (tableId >= 0) {
                Object currentValue = tableModel.getValueAt(modelRow, 1); // Получаем значение из колонки data
                String currentDataText = currentValue.toString();
                
                String newDataText = JOptionPane.showInputDialog(this, "Редактирование данных:", currentDataText);
                if (newDataText != null) {
                    byte[] newDataBytes = newDataText.getBytes(StandardCharsets.UTF_8);
                    
                    boolean success = database.overwriteDataByTableId(tableName, tableId, newDataBytes);
                    if (success) {
                        statusLabel.setText("Данные в таблице " + tableName + " обновлены");
                        loadTableData(tableName, null);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Не удалось обновить данные в таблице " + tableName, 
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Не удалось получить ID строки в таблице " + tableName, 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Ошибка при редактировании данных: " + ex.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteTableRow(String tableName, int rowIndex) {
        try {
            int modelRow = rowIndex;
            if (dataTable.getRowSorter() != null) {
                modelRow = dataTable.getRowSorter().convertRowIndexToModel(rowIndex);
            }
            
            boolean success = database.deleteDataByIndex(tableName, modelRow);
            if (success) {
                statusLabel.setText("Строка удалена из таблицы " + tableName);
                loadTableData(tableName, null);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Не удалось удалить строку из таблицы " + tableName, 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Ошибка при удалении строки: " + ex.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Object tryToDeserializeObject(byte[] bytes) {
        try {
            return SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(bytes);
            return jsonNode.toString();
        } catch (Exception e) {
        }
        
        try {
            String utf8Text = new String(bytes, StandardCharsets.UTF_8);
            if (isPrintableText(utf8Text)) {
                return utf8Text;
            }
        } catch (Exception ignored) {}
        
        return null;
    }
    
    private boolean isPrintableText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        int printableChars = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || isPunctuation(c)) {
                printableChars++;
            }
        }
        
        return (double) printableChars / text.length() > 0.8;
    }
    
    private boolean isPunctuation(char c) {
        return c == '.' || c == ',' || c == '!' || c == '?' || c == ';' || c == ':' || 
               c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' || 
               c == '<' || c == '>' || c == '/' || c == '\\' || c == '\'' || c == '"' || 
               c == '-' || c == '+' || c == '=' || c == '_' || c == '@' || c == '#' || 
               c == '$' || c == '%' || c == '^' || c == '&' || c == '*';
    }
    
    private void closeConnection() {
        if (database != null) {
            database.close();
            database = null;
        }
        
        if (connection != null) {
            try {
                connection.close();
                statusLabel.setText("Соединение закрыто");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
