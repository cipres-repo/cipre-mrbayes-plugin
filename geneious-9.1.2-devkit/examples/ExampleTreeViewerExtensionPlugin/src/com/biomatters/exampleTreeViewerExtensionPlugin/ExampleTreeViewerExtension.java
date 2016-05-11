package com.biomatters.exampleTreeViewerExtensionPlugin;

import com.biomatters.geneious.publicapi.plugin.TreeViewerExtension;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.components.GTextField;

import javax.swing.*;

import jebl.evolution.trees.*;
import jebl.evolution.graphs.Node;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Matt Kearse
 * @version $Id: ExampleTreeViewerExtension.java 68996 2015-08-06 00:03:04Z richard $
 */

public class ExampleTreeViewerExtension extends TreeViewerExtension {
    AnnotatedPluginDocument annotatedPluginDocument;
    private JPanel panel;
    private Tree tree;
    private JLabel statistics1;
    private JLabel statistics2;
    private JLabel statistics3;
    private JTextField attributeName;
    private Set<Node> selectedNodes=new HashSet<Node>();
    private JButton addAttributeButton;

    public ExampleTreeViewerExtension(AnnotatedPluginDocument annotatedPluginDocument) {
        this.annotatedPluginDocument = annotatedPluginDocument;
        panel = new JPanel(new BorderLayout());
        addAttributeButton =new JButton("Add attribute");
        addAttributeButton.setEnabled(false);
        addAttributeButton.setToolTipText("Add's the given attribute name to all selected nodes.");
        attributeName = new GTextField("My Label");
        JPanel firstLine = new JPanel(new BorderLayout());
        firstLine.add(addAttributeButton,BorderLayout.WEST);
        firstLine.add(attributeName,BorderLayout.CENTER);
        attributeName.setPreferredSize(attributeName.getPreferredSize());
        addAttributeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tree newTree=Utils.copyTree(Utils.rootTheTree(tree), false);
                String attribute = attributeName.getText();
                for (Node node : tree.getExternalNodes()) {
                    Node newNode = newTree.getNode(tree.getTaxon(node));
                    if (selectedNodes.contains(node)) {
                        newNode.setAttribute(attribute,attribute);
                    }
                    else {
                        newNode.setAttribute(attribute,"");
                    }
                }
                fireTreeChanged(new TreeChangeEvent(newTree));
            }
        });
        JButton invertSelectionButton=new JButton("Invert selection");
        firstLine.add(invertSelectionButton,BorderLayout.EAST);
        invertSelectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Set<Node> nodes = new HashSet<Node>(tree.getNodes());
                nodes.removeAll(selectedNodes);
                fireSelectionChanged(new TreeSelectionChangeEvent(nodes));
            }
        });
        panel.add(firstLine,BorderLayout.NORTH);
        statistics1 = new JLabel();
        statistics2 = new JLabel();
        statistics3 = new JLabel();
        JPanel statsPanel=new JPanel(new GridLayout(3,1));
        statsPanel.add(statistics1);
        statsPanel.add(statistics2);
        statsPanel.add(statistics3);
        panel.add(statsPanel,BorderLayout.CENTER);
    }

    public String getPanelTitle() {
        return "Example Statistics";
    }

    public JPanel getPanel() {
        return panel;
    }

    public void treeChanged(TreeChangeEvent treeChangeEvent) {
        tree=treeChangeEvent.getTree();
        updateStatistics();
    }

    public void selectionChanged(TreeSelectionChangeEvent treeChangeEvent) {
        selectedNodes=treeChangeEvent.getSelectedNodes();
        addAttributeButton.setEnabled(selectedNodes.size()>0);
        updateStatistics();
    }

    private void updateStatistics() {
        statistics1.setText("Internal nodes: "+tree.getInternalNodes().size());
        statistics2.setText("External nodes: "+tree.getExternalNodes().size());
        statistics3.setText("Selected nodes: "+selectedNodes.size());
    }
}
