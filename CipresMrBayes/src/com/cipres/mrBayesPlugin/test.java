package com.cipres.mrBayesPlugin;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class test {
    private static final int BOOLEAN_COLUMN = 2;

    public test() {
        JTable table = createTable();

        JFrame frame = new JFrame();
        frame.add(new JScrollPane(table));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JTable createTable() {
        String[] cols = {"COL", "COL", "COL"};
        Object[][] data = {{"Hello", "Hello", false}, {"Hello", "Hello", false}};
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override
            public Class getColumnClass(int column) {
                return column == BOOLEAN_COLUMN ? Boolean.class : String.class;
            }
        };
        JTable table = new JTable(model);
        return table;
    }



    public static void main(String[] args) {
       new test();
    }
}