package org.suchard.gui.posterior;


import com.biomatters.geneious.publicapi.utilities.GuiUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * @author Richard
 * @version $Id: TraceSelectionPanel.java 43609 2011-07-28 03:19:13Z matthew $
 */
final class TraceSelectionPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 0L;
	private final JList traceList;

    public TraceSelectionPanel(final GeneiousTraces traces, ListSelectionListener selectionListener) {
        super(new BorderLayout(5, 5));
        setOpaque(false);
        traceList = new JList() {
            public String getToolTipText(MouseEvent evt) {
                int index = locationToIndex(evt.getPoint());
                return traces.getTrace(index).getDescription();
            }
        };
        traceList.setModel(new TraceListModel(traces));
        traceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        traceList.addListSelectionListener(selectionListener);
        traceList.setCellRenderer(new TraceCellRenderer());
        JLabel label = new JLabel("Select Statistics:");
        final JTextArea text = new JTextArea();
        text.setEditable(false);
        String key = "Ctrl";
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            key = "" + GuiUtilities.COMMAND_KEY_CHAR;//this is a mac only character so dont stress if it doesnt show up on your computer
        }
        text.setText("Hold " + key + " key and click to select multiple statistics");
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEnabled(false);
        text.setBorder(new EmptyBorder(0, 5, 0, 0));
        text.setVisible(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(traceList, BorderLayout.CENTER);
        panel.add(text, BorderLayout.SOUTH);
        JScrollPane scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        traceList.addMouseListener(new MouseAdapter() {
            private boolean isInPanel = false;

            public void mouseEntered(MouseEvent e) {
                isInPanel = true;
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e1) {
                            //dont care
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                if (isInPanel) {
                                    text.setVisible(true);
                                }
                            }
                        });
                    }
                }).start();
            }

            public void mouseExited(MouseEvent e) {
                isInPanel = false;
                text.setVisible(false);
            }
        });
        add(scroll, BorderLayout.CENTER);
        add(label, BorderLayout.NORTH);
    }

    public int[] getSelection() {
        return traceList.getSelectedIndices();
    }

    public void setSelection(int[] selection) {
        if (Arrays.equals(selection, getSelection())) {
            return;
        }
        traceList.setSelectedIndices(selection);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(140, 200);
    }

//    @Override
//    public Dimension getMinimumSize() {
//        return new Dimension(130, 200);
//    }

    private static class TraceListModel implements ListModel {
        private GeneiousTraces traces;

        public TraceListModel(GeneiousTraces traces) {
            this.traces = traces;
        }

        public int getSize() {
//        	if( traces == null )
//        		System.out.println("traces == null");
        	//System.exit(-1);
            return traces.getTraceCount();
        }

        public Object getElementAt(int index) {
            return traces.getTrace(index).getName();
        }

        public void addListDataListener(ListDataListener l) {}
        public void removeListDataListener(ListDataListener l) {}
    }

    private static class TraceCellRenderer implements ListCellRenderer {

        public static final Color BG1 = new Color(0xED, 0xF3, 0xFE);
        public static final Color BG2 = Color.WHITE;
        private final Color foreground;

        private final JLabel component = new JLabel();

        public TraceCellRenderer() {
            component.setOpaque(true);
            foreground = component.getForeground();
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            component.setText(value.toString());

            if (isSelected) {
                component.setBackground(list.getSelectionBackground());
                component.setForeground(list.getSelectionForeground());
            } else {
                if (index % 2 == 0) {
                    component.setBackground(BG1);
                } else {
                    component.setBackground(BG2);
                }
                component.setForeground(foreground);
            }

            return component;
        }
    }
}
