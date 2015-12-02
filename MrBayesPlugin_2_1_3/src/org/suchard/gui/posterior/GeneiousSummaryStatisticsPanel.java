package org.suchard.gui.posterior;

import dr.app.tracer.*;
import org.virion.jam.util.PrintUtilities;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;


public class GeneiousSummaryStatisticsPanel extends SummaryStatisticsPanel implements Printable {

	/**
	 *
	 */
	private static final long serialVersionUID = 297818066005588509L;
	static final String NAME_ROW = "name";
	static final String MEAN_ROW = "mean";
	static final String STDEV_ROW = "stdev of mean";
	static final String MEDIAN_ROW = "median";
	static final String LOWER_ROW = "95% HPD lower";
	static final String UPPER_ROW = "95% HPD upper";
	static final String ACT_ROW = "auto-correlation time (ACT)";
	static final String ESS_ROW = "effective sample size (ESS)";
	static final String SUM_ESS_ROW = "effective sample size (sum of ESS)";

	GeneiousCombinedTraces combinedTraces = null;
	int[] combinedTraceIndices = null;

	TraceList traceList = null;
	int[] traceIndices = null;

	StatisticsModel statisticsModel;
	JTable statisticsTable = null;
	JScrollPane scrollPane1 = null;
	JPanel topPanel = null;
	JSplitPane splitPane1 = null;

	int dividerLocation = -1;

	FrequencyPanel frequencyPanel = null;
	IntervalsPanel intervalsPanel = null;
	JComponent currentPanel = null;

	private final ExtraStatistic[] extraStatistics = new ExtraStatistic[]{
			new ExtraStatistic("Total Chain Length") {
				protected String getValue() {
					//todo get the real current value from traceList or whatever
					if (traceList != null)
						return Integer.toString(
								((GeneiousTraces) traceList).getMaxState());
						//	.getStateCount()*traceList.getStepSize());
					else
						return "";
					//return "10";
				}
			},
			new ExtraStatistic("Subsample Freq") {
				protected String getValue() {
					if (traceList != null)
						return Integer.toString(((GeneiousTraces) traceList).getStepSize());
					else
						return "";
					//return "100";
				}
			},
			new ExtraStatistic("Burn-in Length") {
				protected String getValue() {
					if (traceList != null)
						return Integer.toString(
								((GeneiousTraces) traceList).getBurnIn()
						);
					else
						return "";
				}
			},
			new ExtraStatistic("Samples Analyzed") {
				protected String getValue() {
					if (traceList != null)
						return Integer.toString(
								((GeneiousTraces) traceList).getStateCount()
						);
					else
						return "";
				}
			}
	};
	private JPanel extraStatisticsPanel;

	public GeneiousSummaryStatisticsPanel() {

		setOpaque(false);

		statisticsModel = new StatisticsModel();
		statisticsTable = new JTable(statisticsModel);

		statisticsTable.getColumnModel().getColumn(0).setCellRenderer(
				new TableRenderer(SwingConstants.RIGHT, 4));
		statisticsTable.getColumnModel().getColumn(1).setCellRenderer(
				new TableRenderer(SwingConstants.LEFT, 4));

		currentPanel = statisticsTable;
		scrollPane1 = new JScrollPane(statisticsTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		topPanel = new JPanel(new BorderLayout(5, 0));
		topPanel.setOpaque(false);
		topPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(0, 0, 6, 0)));
		topPanel.add(scrollPane1, BorderLayout.CENTER);

		extraStatisticsPanel = createExtraStatisticsPanel();
		topPanel.add(extraStatisticsPanel, BorderLayout.WEST);

		frequencyPanel = new FrequencyPanel();
		frequencyPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(6, 0, 0, 0)));

		intervalsPanel = new IntervalsPanel();
		intervalsPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(6, 0, 0, 0)));

		splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, frequencyPanel);
		splitPane1.setOpaque(false);
		splitPane1.setBorder(null);
//		splitPane1.setBorder(new BorderUIResource.EmptyBorderUIResource(
//								new java.awt.Insets(12, 12, 12, 12)));

		setLayout(new BorderLayout(0, 0));
		add(splitPane1, BorderLayout.CENTER);

		splitPane1.setDividerLocation(2000);

	}

	private JPanel createExtraStatisticsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 8, 3, 3);
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.BOTH;
		for (ExtraStatistic extraStatistic : extraStatistics) {
			c.gridwidth = 1;
			panel.add(new JLabel(extraStatistic.getLabel() + ":"), c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			JLabel valueLabel = new JLabel(extraStatistic.getValue());
			panel.add(valueLabel, c);
			extraStatistic.setValueLabel(valueLabel);
		}
		c.weighty = 1.0;
		JPanel spacer = new JPanel();
		spacer.setOpaque(false);
		panel.add(spacer, c);
		return panel;
	}

	private void setupDividerLocation() {

		if (dividerLocation == -1 || dividerLocation == splitPane1.getDividerLocation()) {
			int h0 = topPanel.getHeight();
			int h1 = scrollPane1.getViewport().getHeight();
			if (h0 == 0) {
				h0 = scrollPane1.getHorizontalScrollBar().getPreferredSize().height * 3;
			}
			int h2 = statisticsTable.getPreferredSize().height;
			dividerLocation = h2 + h0 - h1;

			splitPane1.setDividerLocation(dividerLocation);
		}
	}

	public void setCombinedTraces(GeneiousCombinedTraces combinedTraces, int[] traces) {

		this.combinedTraces = combinedTraces;
		this.combinedTraceIndices = traces;

		this.traceList = null;
		this.traceIndices = null;

		statisticsModel.fireTableStructureChanged();
		for (ExtraStatistic extraStatistic : extraStatistics) {
			extraStatistic.valueChanged();
		}

		if (combinedTraces != null && traces != null && traces.length == 1) {
			frequencyPanel.setTrace(combinedTraces, traces[0]);
		} else {
			frequencyPanel.setTrace(null, -1);
		}

		statisticsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		statisticsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		statisticsTable.getColumnModel().getColumn(0).setCellRenderer(
				new TableRenderer(SwingConstants.RIGHT, 4));
		for (int i = 1; i < statisticsTable.getColumnCount(); i++) {
			statisticsTable.getColumnModel().getColumn(i).setPreferredWidth(100);
			statisticsTable.getColumnModel().getColumn(i).setCellRenderer(
					new TableRenderer(SwingConstants.LEFT, 4));
		}

		setupDividerLocation();

		validate();
		repaint();
	}

	public void setTraces(TraceList traceList, int[] traces) {
		this.traceList = traceList;
		this.traceIndices = traces;

		this.combinedTraces = null;
		this.combinedTraceIndices = null;

		int divider = splitPane1.getDividerLocation();

		statisticsModel.fireTableStructureChanged();
		for (ExtraStatistic extraStatistic : extraStatistics) {
			extraStatistic.valueChanged();
		}

		if (traceList != null && traces != null) {
			if (traces.length == 1) {
				statisticsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

				currentPanel = frequencyPanel;
				frequencyPanel.setTrace(traceList, traces[0]);
				intervalsPanel.setTraces(null, null);
				splitPane1.setBottomComponent(frequencyPanel);
			} else {
				statisticsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				statisticsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
				for (int i = 1; i < statisticsTable.getColumnCount(); i++) {
					statisticsTable.getColumnModel().getColumn(i).setPreferredWidth(100);
				}

				currentPanel = intervalsPanel;
				frequencyPanel.setTrace(null, -1);
				intervalsPanel.setTraces(traceList, traces);
				splitPane1.setBottomComponent(intervalsPanel);
			}
		} else {
			currentPanel = statisticsTable;
			frequencyPanel.setTrace(null, -1);
			splitPane1.setBottomComponent(frequencyPanel);
		}

		splitPane1.setDividerLocation(divider);

		statisticsTable.getColumnModel().getColumn(0).setCellRenderer(
				new TableRenderer(SwingConstants.RIGHT, 4));
		for (int i = 1; i < statisticsTable.getColumnCount(); i++) {
			statisticsTable.getColumnModel().getColumn(i).setCellRenderer(
					new TableRenderer(SwingConstants.LEFT, 4));
		}

		setupDividerLocation();

		validate();
		repaint();
	}

	public JComponent getExportableComponent() {
		return currentPanel;
	}

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		PageFormat format1 = (PageFormat) pageFormat.clone();
		Paper paper1 = format1.getPaper();
		double tableProportion = 0.2; //vertical
		double statisticsProportion = 0.2; //horizontal
		double citationProportion = 0.1; //vertical
		paper1.setImageableArea(
				paper1.getImageableX() + paper1.getImageableWidth() * statisticsProportion,
				paper1.getImageableY(),
				paper1.getImageableWidth() * (1 - statisticsProportion),
				paper1.getImageableHeight() * tableProportion);
		paper1.setSize(paper1.getWidth(), paper1.getHeight());
		format1.setPaper(paper1);
        final Printable printable = statisticsTable.getPrintable(JTable.PrintMode.FIT_WIDTH, null, null);
        printable.print(graphics.create(), format1, pageIndex);

        PageFormat format3 = (PageFormat) pageFormat.clone();
		Paper paper3 = format3.getPaper();
		paper3.setImageableArea(
				paper3.getImageableX(),
				paper3.getImageableY(),
				paper3.getImageableWidth() * statisticsProportion,
				paper3.getImageableHeight() * tableProportion);
		paper3.setSize(paper3.getWidth(), paper3.getHeight());
		format3.setPaper(paper3);
		PrintUtilities.printScaled(extraStatisticsPanel, graphics.create(), format3, pageIndex);


		PageFormat format2 = (PageFormat) pageFormat.clone();
		Paper paper2 = format2.getPaper();
		paper2.setImageableArea(
				paper2.getImageableX(),
				paper2.getImageableY() + paper2.getImageableHeight() * tableProportion,
				paper2.getImageableWidth(),
				paper2.getImageableHeight() * (1 - tableProportion - citationProportion));
		paper2.setSize(paper2.getWidth(), paper2.getHeight());
		format2.setPaper(paper2);
		PrintUtilities.printScaled(currentPanel, graphics, format2, pageIndex);

		PageFormat format4 = (PageFormat) pageFormat.clone();
		Paper paper4 = format4.getPaper();
		Component citationText = PosteriorDocument.Utilities.getViewerCitationText();
		JDialog d = new JDialog();
		d.getContentPane().add(citationText, BorderLayout.CENTER);
		d.pack();
		double ratio = d.getPreferredSize().getWidth() / d.getPreferredSize().getHeight();
		double width = paper4.getImageableHeight() * citationProportion * ratio;
		width *= 0.5;
		paper4.setImageableArea(
				paper4.getImageableX() + paper4.getImageableWidth() / 2 - width,
				paper4.getImageableY() + paper4.getImageableHeight() * (1 - citationProportion),
				paper4.getImageableWidth(),
				paper4.getImageableHeight() * citationProportion);
		paper4.setSize(paper4.getWidth(), paper4.getHeight());
		format4.setPaper(paper4);
		return PrintUtilities.printScaled(citationText, graphics, format4, pageIndex);
	}

	class StatisticsModel extends AbstractTableModel {

		/**
		 *
		 */
		private static final long serialVersionUID = 8646434743159874965L;

		String[] rowNames = {MEAN_ROW, STDEV_ROW, MEDIAN_ROW, LOWER_ROW, UPPER_ROW, ACT_ROW, ESS_ROW};

		private DecimalFormat formatter = new DecimalFormat("0.###E0");
		private DecimalFormat formatter2 = new DecimalFormat("####0.###");

		public StatisticsModel() {
		}

		public int getColumnCount() {
			if (combinedTraces != null && combinedTraceIndices != null) {
				if (combinedTraceIndices.length == 1) {
					return combinedTraces.getTraceListCount() + 2;
				} else {
					return combinedTraceIndices.length + 1;
				}
			} else if (traceList != null && traceIndices != null) {
				return traceIndices.length + 1;
			} else {
				return 2;
			}
		}

		public int getRowCount() {
			return rowNames.length;
		}

		public Object getValueAt(int row, int col) {

			if (col == 0) {
				return rowNames[row];
			}

			TraceDistribution td = null;
			TraceCorrelation tc = null;

			if (combinedTraces != null && combinedTraceIndices != null) {
				if (combinedTraceIndices.length == 1) {
					if (col == 1) {
						td = combinedTraces.getDistributionStatistics(combinedTraceIndices[0]);
					} else {
						TraceList tl = combinedTraces.getTraceList(col - 2);

						tc = tl.getCorrelationStatistics(combinedTraceIndices[0]);
						td = tc;
					}
				} else {
					td = combinedTraces.getDistributionStatistics(combinedTraceIndices[col - 1]);
				}

			} else if (traceList != null && traceIndices != null) {
//				System.out.println("I am here");
				tc = traceList.getCorrelationStatistics(traceIndices[col - 1]);
				td = tc;
			} else {
				return "-";
			}

			double value = 0.0;

			if (tc != null) {
				if (row != 0 && !tc.isValid()) return "n/a";

				switch (row) {
					case 0:
						value = tc.getMean();
						break;
					case 1:
						value = tc.getStdErrorOfMean();
						break;
					case 2:
						value = tc.getMedian();
						break;
					case 3:
						value = tc.getLowerHPD();
						break;
					case 4:
						value = tc.getUpperHPD();
						break;
					case 5:
						value = tc.getACT();
						break;
					case 6:
						value = tc.getESS();
						break;
				}
			} else if (td != null) {
				if (row != 0 && !td.isValid()) return "n/a";

				switch (row) {
					case 0:
						value = td.getMean();
						break;
					case 1:
						return "n/a";
					case 2:
						value = td.getMedian();
						break;
					case 3:
						value = td.getLowerHPD();
						break;
					case 4:
						value = td.getUpperHPD();
						break;
					case 5:
						return "n/a";
					case 6:
						value = td.getESS();
						break;
				}
			} else {
				return "-";
			}

			if (value > -100 && (Math.abs(value) < 0.1 || Math.abs(value) >= 10000.0)) {
				return formatter.format(value);
			} else {
				return formatter2.format(value);
			}
		}

		public String getColumnName(int column) {
			if (column == 0) return "Summary Statistic";
			if (combinedTraces != null && combinedTraceIndices != null) {
				if (combinedTraceIndices.length == 1) {
					if (column == 1) {
						return "Combined";
					} else {
						TraceList tl = combinedTraces.getTraceList(column - 2);
						return tl.getName();
					}
				} else {
					return combinedTraces.getTraceName(combinedTraceIndices[column - 1]);
				}
			} else if (traceList != null && traceIndices != null) {
				return traceList.getTraceName(traceIndices[column - 1]);
			}
			return "-";
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();

			buffer.append(getColumnName(0));
			for (int j = 1; j < getColumnCount(); j++) {
				buffer.append("\t");
				buffer.append(getColumnName(j));
			}
			buffer.append("\n");

			for (int i = 0; i < getRowCount(); i++) {
				buffer.append(getValueAt(i, 0));
				for (int j = 1; j < getColumnCount(); j++) {
					buffer.append("\t");
					buffer.append(getValueAt(i, j));
				}
				buffer.append("\n");
			}

			return buffer.toString();
		}
	}

	private abstract static class ExtraStatistic {
		protected String label;
		private JLabel valueLabel;

		public ExtraStatistic(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		protected abstract String getValue();

		public void setValueLabel(JLabel valueLabel) {
			this.valueLabel = valueLabel;
		}

		public void valueChanged() {
			if (valueLabel != null) {
				valueLabel.setText(getValue());
			} else {
				//a bug if this is null, but its non-fatal
				assert false : "value changed before label set!";
			}
		}
	}
}
