package org.suchard.gui.posterior;

import com.biomatters.geneious.publicapi.documents.URN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A 
 *
 * @author Richard
 * @version $Id: TraceSelectionManager.java 43609 2011-07-28 03:19:13Z matthew $
 */
final class TraceSelectionManager {

    private static final Map<List<URN>, int[]> SELECTIONS = new HashMap<List<URN>, int[]>();
    private static final Map<List<URN>, Integer> BURNINS = new HashMap<List<URN>, Integer>();
    public static final int DEFAULT_BURNIN = 10;

    /**
     * Should be called when the user changes the selection of traces. Other trace viewers
     * on the same documents can then have the same trace selection.
     *
     * @param urns of the currently selected documents
     * @param selection of traces for those documents.
     */
    public static void setSelection(List<URN> urns, int[] selection) {
        SELECTIONS.put(urns, selection);
        fireSelectionChanged();
    }

    public static void setBurnin(List<URN> urns, int burnin) {
        BURNINS.put(urns, burnin);
//        System.out.println("save burnin: " + burnin);
        fireBurninChanged();
    }

    private static void fireBurninChanged() {
        ArrayList<SelectionListener> copy;
        synchronized(SELECTION_LISTENERS) {
            copy = new ArrayList<SelectionListener>(SELECTION_LISTENERS);
        }
        for (SelectionListener selectionListener : copy) {
            selectionListener.burninChanged();
        }
    }

    public static void fireSelectionChanged() {
        ArrayList<SelectionListener> copy;
        synchronized(SELECTION_LISTENERS) {
            copy = new ArrayList<SelectionListener>(SELECTION_LISTENERS);
        }
        for (SelectionListener selectionListener : copy) {
            selectionListener.selectionChanged();
        }
    }

    /**
     * get the last set of selected traces (by indices) for the selected documents
     * (by urn). returns a single element array containing 0 if no selection exists.
     */
    public static int[] getSelection(List<URN> urns) {
        int[] selection = SELECTIONS.get(urns);
        return selection == null ? new int[] {0} : selection;
    }

    public static int getBurnin(List<URN> urns) {
        Integer burnin = BURNINS.get(urns);
        return burnin == null ? DEFAULT_BURNIN : burnin;
    }

//    private static final SimpleListenerManager SELECTION_LISTENERS = new SimpleListenerManager();
    private static final List<SelectionListener>  SELECTION_LISTENERS = new ArrayList<SelectionListener>();

    /**
     * add a listener which is fired every time the trace selection is set. These
     * must be carefully removed as they are not weakly referenced and can cause
     * memory leaks. Use NoLongerViewedListener to remove these.
     * <p/>
     * Used so viewers on the same documents can find out when the trace selection
     * is changed.
     */
    public static void addChangeListener(SelectionListener listener) {
        synchronized (SELECTION_LISTENERS) {
            SELECTION_LISTENERS.add(listener);
        }
    }

    /**
     * remove a selection listener
     */
    public static void removeChangeListener(SelectionListener listener) {
        synchronized (SELECTION_LISTENERS) {
            SELECTION_LISTENERS.remove(listener);
        }
    }
}