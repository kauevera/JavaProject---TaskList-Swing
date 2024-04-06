import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class TasksList extends JFrame {
    private final JPanel panel;
    private final JTextField textField;
    private final JComboBox<String> comboBox;
    private final JButton removeButton;
    private final Map<String, ColumnPanel> columnMap;

    public TasksList() {
        setTitle("Tasks List");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null); // Centraliza a janela na tela

        panel = new JPanel(new GridLayout(1, 3)); // Usando GridLayout para dividir em 3 colunas
        columnMap = new HashMap<>();
        //Font timesNewRoman = new Font("Times New Roman", Font.PLAIN, 12);
        //Color whiteColor = Color.WHITE;
        // Font timesNewRomanWhite = timesNewRoman.deriveFont(whiteColor);
        
        ColumnPanel toDoColumn = new ColumnPanel("To Do");
        ColumnPanel doingColumn = new ColumnPanel("Doing");
        ColumnPanel doneColumn = new ColumnPanel("Done");
        panel.add(toDoColumn);
        panel.add(doingColumn);
        panel.add(doneColumn);
        columnMap.put("To Do", toDoColumn);
        columnMap.put("Doing", doingColumn);
        columnMap.put("Done", doneColumn);

        add(panel, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBackground(new Color(52, 34, 121));

        textField = new JTextField(20);
        inputPanel.add(textField);

        comboBox = new JComboBox<>(new String[]{"To Do", "Doing", "Done"});
        inputPanel.add(comboBox);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });
        inputPanel.add(addButton);

        removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeTask();
            }
        });
        inputPanel.add(removeButton);

        add(inputPanel, BorderLayout.SOUTH);

        // Habilita o suporte a drag-and-drop
        for (ColumnPanel columnPanel : columnMap.values()) {
            columnPanel.taskList.setTransferHandler(new TaskTransferHandler());
            columnPanel.taskList.setDropMode(DropMode.INSERT);
            columnPanel.taskList.setDragEnabled(true);
        }
    }

    private void addTask() {
        String task = textField.getText();
        String selectedColumn = (String) comboBox.getSelectedItem();
        ColumnPanel columnPanel = columnMap.get(selectedColumn);
        if (columnPanel != null) {
            columnPanel.addTaskLayout(task);
        }
        textField.setText(""); // Limpa o campo de texto após adicionar a tarefa
    }

    private void removeTask() {
        String task = textField.getText();
        String selectedColumn = (String) comboBox.getSelectedItem();
        ColumnPanel columnPanel = columnMap.get(selectedColumn);
        if (columnPanel != null) {
            columnPanel.removeSelectedTaskLayout();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TasksList myTaskList = new TasksList();
            myTaskList.setVisible(true);
        });
    }
}

class ColumnPanel extends JPanel {
    private final String titulo;
    JList<String> taskList;

    public ColumnPanel(String titulo) {
        this.titulo = titulo;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(titulo));
        setBackground(new Color(161, 155, 240));

        taskList = new JList<>(new DefaultListModel<>());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(taskList);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addTaskLayout(String task) {
        DefaultListModel<String> model = (DefaultListModel<String>) taskList.getModel();
        model.addElement(task);
    }

    public void removeSelectedTaskLayout() {
        DefaultListModel<String> model = (DefaultListModel<String>) taskList.getModel();
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            model.remove(selectedIndex);
        }
    }
}

class TaskTransferHandler extends TransferHandler {
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JList<String> list = (JList<String>) c;
        String value = list.getSelectedValue();
        return new StringTransferable(value);
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (action == TransferHandler.MOVE) {
            JList<String> list = (JList<String>) source;
            DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex != -1) {
                model.remove(selectedIndex);
            }
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
        int index = dl.getIndex();
        JList<String> list = (JList<String>) support.getComponent();
        DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();

        try {
            String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            model.add(index, data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

class StringTransferable implements Transferable {
    private final String data;

    public StringTransferable(String data) {
        this.data = data;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.stringFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.stringFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
        return data;
    }
}