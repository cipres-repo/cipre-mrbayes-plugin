package org.suchard.gui;

import javax.swing.*;
import java.awt.*;

public class OptionsPanel extends JPanel {

	private static final long serialVersionUID = 2233160553241459637L;

	private GridBagConstraints constraints = new GridBagConstraints();

	private int verticalGap = 2;

	private JPanel panel = new JPanel();
	private double leftWeight = 1.0;
	private int labelAnchor = GridBagConstraints.EAST;

	public OptionsPanel(boolean inScrollPane, boolean leftComponentHasMoreSpaceThanRequired) {
		setLeftComponentHasMoreSpaceThanRequired(leftComponentHasMoreSpaceThanRequired);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		setOpaque(false);
		setLayout(new BorderLayout());
		if (inScrollPane) {
			JScrollPane scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setBorder(null);
			scroll.setOpaque(false);
			scroll.getViewport().setOpaque(false);
			add(scroll, BorderLayout.CENTER);
		} else {
			add(panel, BorderLayout.CENTER);
		}
		constraints.weighty = 0.0;
	}

	/**
	 * sets the size of the gap underneath added componenents (there is still a 2 pixel gap above).
	 * Default is 2.
	 */
	public void setVerticalGap(int gap) {
		this.verticalGap = gap;
	}

	/**
	 * set whether the left components (normally labels) take up more space than required
	 * default true
	 *
	 * @param hasEqualWeightWithRight if false, the left components will only take up the minimum necessary space.
	 */
	public void setLeftComponentHasMoreSpaceThanRequired(boolean hasEqualWeightWithRight) {
		leftWeight = hasEqualWeightWithRight ? 1.0 : 0.0;
	}

	public void addLabel(String string) {
		JLabel jLabel = new JLabel(string);
		jLabel.setOpaque(false);
		jLabel.setForeground(Color.gray.darker());
		addComponent(jLabel, false);
	}

	public void addComponent(JComponent comp, boolean fillAvailableHorizontalSpace) {
		JPanel empty = new JPanel();
		empty.setOpaque(false);
		constraints.insets = new Insets(2, 13, verticalGap, 0);
		constraints.weightx = leftWeight;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		panel.add(empty, constraints);
		constraints.insets = new Insets(2, 5, verticalGap, 8);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.anchor = GridBagConstraints.WEST;
		if (fillAvailableHorizontalSpace) {
			constraints.fill = GridBagConstraints.HORIZONTAL;
		}
		panel.add(comp, constraints);
	}

	/**
	 * @param label
	 * @param comp
	 * @param fillAvailableHorizontalSpace
	 * @return the label which was added
	 */
	public JLabel addComponentWithLabel(String label, JComponent comp, boolean fillAvailableHorizontalSpace) {
		JLabel jLabel = new JLabel(label);
		jLabel.setOpaque(false);
		constraints.insets = new Insets(2, 13, verticalGap, 0);
		constraints.weightx = leftWeight;
		constraints.gridwidth = 1;
		constraints.anchor = labelAnchor;
		constraints.fill = GridBagConstraints.NONE;
		panel.add(jLabel, constraints);
		constraints.insets = new Insets(2, 5, verticalGap, 8);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.anchor = GridBagConstraints.WEST;
		if (fillAvailableHorizontalSpace) {
			constraints.fill = GridBagConstraints.HORIZONTAL;
		}
		panel.add(comp, constraints);
		jLabel.setLabelFor(comp);
		return jLabel;
	}

	public void addSpanningComponent(JComponent comp, boolean fillHorizontalSpace) {
		constraints.insets = new Insets(2, 8, verticalGap, 8);
		if (fillHorizontalSpace) {
			constraints.fill = GridBagConstraints.BOTH;
		} else {
			constraints.fill = GridBagConstraints.VERTICAL;
		}
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(comp, constraints);
	}

	public void addGap() {
		JPanel empty = new JPanel();
		empty.setOpaque(false);
		addSpanningComponent(empty, true);
	}

	public void addDividerWithLabel(String label) {
		JPanel labelPanel = new JPanel(new GridBagLayout());
		labelPanel.setOpaque(false);
		JPanel divider1 = new JPanel();
		divider1.setBackground(Color.gray);
		divider1.setPreferredSize(new Dimension(1, 1));
		divider1.setMinimumSize(new Dimension(1, 1));
		JLabel jLabel = new JLabel(label);
		jLabel.setOpaque(false);
		jLabel.setForeground(Color.gray.darker());
		constraints.weightx = 0.0;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.anchor = GridBagConstraints.SOUTH;
		labelPanel.add(jLabel, constraints);
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(0, 0, 0, 0);
		labelPanel.add(divider1, constraints);

		JPanel divider2 = new JPanel();
		divider2.setBackground(Color.gray);
		divider2.setPreferredSize(new Dimension(1, 1));
		divider2.setMinimumSize(new Dimension(1, 1));

		constraints.gridwidth = 1;
		constraints.insets = new Insets(14, 16, 16, 0);
		constraints.weightx = leftWeight;
		panel.add(labelPanel, constraints);
		constraints.weightx = 1.0;
		constraints.insets = new Insets(14, 0, 16, 16);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(divider2, constraints);
		constraints.anchor = GridBagConstraints.CENTER;
	}

	@Override
	public void removeAll() {
		panel.removeAll();
	}


}

