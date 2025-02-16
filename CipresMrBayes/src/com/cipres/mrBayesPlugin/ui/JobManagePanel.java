package com.cipres.mrBayesPlugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.ngbw.directclient.CiCipresException;
import org.ngbw.directclient.CiClient;
import org.ngbw.directclient.CiJob;

import com.cipres.mrBayesPlugin.CipresMrBayesToolbar;
import com.cipres.mrBayesPlugin.models.UserModel;
import com.cipres.mrBayesPlugin.models.UserModel.Job;
import com.cipres.mrBayesPlugin.utilities.CipresUtilities;
import com.cipres.mrBayesPlugin.utilities.DataHandlingUtilities;

/**
 * Display user's jobs and allow users to submit/modify jobs
 * @author rjzheng
 *
 */
@SuppressWarnings("serial")
public class JobManagePanel extends JPanel implements ActionListener, TableModelListener{
	
	public static JTable table;
	
	public static TableModel table_model;
	
	public static List<String> selected_jobs = new ArrayList<String>();
	
	public static Boolean fire = true;

	public static TableModel getTable_model() {
		return table_model;
	}

	public static void setTable_model(TableModel table_model) {
		JobManagePanel.table_model = table_model;
	}

	public static JTable getTable() {
		return table;
	}

	public static void setTable(JTable table) {
		JobManagePanel.table = table;
	}

	/**
	 * Create and layout the job list table and buttons
	 * @param json
	 * @return panel
	 */
	public JPanel createPanel(JSONArray json){

		//Create the Panel
    	JPanel panel = new JPanel();
    	//Add layout to panel
    	GroupLayout layout = new GroupLayout(panel);
    	//Create the buttons
    	JButton submitButton = new JButton("Submit Job");
    	JButton updateButton = new JButton("Update List");
	    JButton downloadButton = new JButton("Download Job");
	    JButton deleteButton = new JButton("Delete Job");
	    updateButton.addActionListener(this);
	    submitButton.addActionListener(this);
	    updateButton.addActionListener(this);
	    deleteButton.addActionListener(this);
	    //Create the table
	    table = createTable(json);
	    table.getModel().addTableModelListener(this);
	    //Add the table to a scroll panel
	    JScrollPane scroll = new JScrollPane(table);
	    //Set the layout
    	panel.setLayout(layout);
	    layout.setAutoCreateContainerGaps(true);
	    layout.setAutoCreateGaps(true);
    	
	    //Set the horizontal grouping
	    layout.setHorizontalGroup(
	    	layout.createSequentialGroup()
	    		.addComponent(scroll)
	    		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	    			.addComponent(updateButton)
	    			.addComponent(submitButton)
	    			.addComponent(downloadButton)
	    			.addComponent(deleteButton))
		);
	    
	    //Set the vertical grouping
	    layout.setVerticalGroup(
	    	layout.createParallelGroup()
			.addComponent(scroll)
    		.addGroup(layout.createSequentialGroup()
    			.addComponent(updateButton)
    			.addComponent(submitButton)
    			.addComponent(downloadButton)
    			.addComponent(deleteButton))
	    );
	    return panel;
    }
	
	public void tableChanged(TableModelEvent e) {
		if(fire == true){
			int row = e.getFirstRow();
	        int column = e.getColumn();
	        TableModel model = (TableModel)e.getSource();
	        Object data = model.getValueAt(row, column+1);
	        System.out.println(data);
        	selected_jobs.add((String)data);
		}
    }

	
	/**
	 * Create the table with user's job list
	 * @param json
	 * @return table
	 */
    public JTable createTable(JSONArray json){
    	try{
	    	List<Job> jobList = new ArrayList<Job>();
	    	UserModel temp = new UserModel();
	    	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	    	for(int i = 0; i < json.size(); i++){
	    		Job job = temp.new Job();
	    		JSONObject obj = (JSONObject) json.get(i);
	    		job.setSelected(false);
	    		job.setJobName(obj.get("jobName").toString());
	    		job.setDate(df.parse(obj.get("date").toString()));
	    		job.setJobStage(obj.get("jobStage").toString());
	    		jobList.add(job);
	    	}
	    	table_model = new TableModel(jobList);
	    	JTable new_table = new JTable(table_model);
	    	return new_table;
    	}catch (ParseException e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    /**
     * Customized table model
     * @author rjzheng
     *
     */
	static class TableModel extends AbstractTableModel {
		
		//Default table column names
		private String[] columnNames = 
    		{
			"Select",
            "Job Name",
            "Date Submitted",
            "Job Status"
            };
		
		private List<Job> jobList = new ArrayList<Job>();
		
		/**
		 * Empty constructor
		 */
		public TableModel(){}
		
		/**
		 * Constructor with job list input
		 * @param jobs
		 */
		public TableModel(List<Job> jobs){
			this.jobList = jobs;
		}

		/**
		 * Get the column count
		 * @return column count
		 */
        public int getColumnCount() {
          return columnNames.length;
        }

        /**
         * Get the row count
         * @return job list size
         */
        public int getRowCount() {
          return jobList.size();
        }

        /**
         * Get the column name at a given column
         * @param col
         * @return column name
         */
        public String getColumnName(int col) {
          return columnNames[col];
        }
        
        /**
         * Get and set the value of a given row and column
         * @param row
         * @param col
         * @return job object
         */
        public Object getValueAt(int row, int col) {
        	Object jobAttribute = null;
        	Job jobObj = jobList.get(row);
        	switch(col){
        		case 0: jobAttribute = jobObj.getSelected();break;
        		case 1: jobAttribute = jobObj.getJobName(); break;
        		case 2: jobAttribute = jobObj.getDate(); break;
        		case 3: jobAttribute = jobObj.getJobStage(); break;
        	}
        	return jobAttribute;
        }
        
        @Override
        public void setValueAt(Object val, int row, int col)
        {
            Job jobObj = jobList.get(row);
            switch(col){
            	case 0: jobObj.setSelected((Boolean)val); break;
            	case 1: jobObj.setJobName((String)val); break;
            	case 2: jobObj.setDate((Date)val);
            	case 3: jobObj.setJobStage((String)val);
            }
            fireTableCellUpdated(row, col);
        }
        
        public boolean isCellEditable(int row, int column) {
            return column==0;
        }

        /**
         * Display checkbox instead of true/false
         */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int c) {
	    	if(c == 0){
	            return Boolean.class;
	        }else{
	        	return String.class;
	        }
        }

        /**
         * Add job and update changes to table
         * @param job
         */
        public void addJob(Job job){
        	jobList.add(job);
        	fire = false;
        	fireTableDataChanged();
        	fire = true;
        }
        
        public void deleteJob(int index){
        	jobList.remove(index);
        	fire = false;
        	fireTableDataChanged();
        	fire = true;
        }
        
        public void clearTable(){
        	while(!jobList.isEmpty()){
        		this.deleteJob(0);
        	}
        	fire = false;
        	fireTableDataChanged();
        	fire = true;
        }
        
    }
	
	public void updateTable(JSONArray json){
		try {
			table_model.clearTable();
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			UserModel temp = new UserModel();
	    	for(int i = 0; i < json.size(); i++){
	    		Job job = temp.new Job();
	    		JSONObject obj = (JSONObject) json.get(i);
	    		job.setSelected(false);
	    		job.setJobName(obj.get("jobName").toString());
				job.setDate(df.parse(obj.get("date").toString()));
	    		job.setJobStage(obj.get("jobStage").toString());
	    		table_model.addJob(job);
	    	}
		}catch (ParseException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			DataHandlingUtilities handler = DataHandlingUtilities.getInstance();   
			UserModel user = handler.getUser();
			CiClient client = handler.getClient();
			Collection<CiJob> allJobs = client.listJobs();
			
			if(e.getActionCommand() == "Update List"){
				JSONArray retJSONArray = CipresUtilities.updateList(CipresMrBayesToolbar.myClient, user);
				updateTable(retJSONArray);
			} 
			else if(e.getActionCommand() == "Delete Job"){
				CipresUtilities.deleteJobs(selected_jobs, allJobs);
				
				JSONArray retJSONArray = CipresUtilities.updateList(CipresMrBayesToolbar.myClient, user);
				updateTable(retJSONArray);
			}
		
		
		} catch (CiCipresException | org.json.simple.parser.ParseException e1) {
			e1.printStackTrace();
		}
		
	}

}
