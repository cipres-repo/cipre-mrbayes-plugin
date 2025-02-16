package com.cipres.mrBayesPlugin.utilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ngbw.directclient.CiCipresException;
import org.ngbw.directclient.CiClient;
import org.ngbw.directclient.CiJob;

import com.cipres.mrBayesPlugin.models.UserModel;
import com.cipres.mrBayesPlugin.models.UserModel.Job;

/**
 * Utility class that handles any CIPRES actions such as submit, and list jobs
 * @author rjzheng
 *
 */
public class CipresUtilities {
	
	public static Boolean clientCheck(CiClient myClient){
		try {
			myClient.listJobs();
			return true;
		} catch (CiCipresException e) {
			return false;
		}
	}
	
	/**
	 * Fetch and save user's jobs
	 * @param myClient
	 * @param user
	 * @throws CiCipresException
	 */
	public static void listJobs(CiClient myClient, UserModel user) throws CiCipresException
	{
		//Create a handler instance
		DataHandlingUtilities handler = DataHandlingUtilities.getInstance();
		handler.clearJobs();
		
		//Fetch jobs from CIPRES
		Collection<CiJob> jobs = myClient.listJobs(); 
		
		//Save jobs
		for (CiJob job : jobs)
		{
			Job newJob = user.new Job();
			newJob.setJobName(job.getClientJobName());
			newJob.setJobStage(job.getJobStage());
			newJob.setDate(job.getDateSubmitted());
			
			handler.addJob(newJob);
			
		}
		
		//Set user's jobs
		user.setJobs(handler.getJobs());
	}
	
	public static Collection<CiJob> getJobs(CiClient myClient) throws CiCipresException{
		//Create a handler instance
		DataHandlingUtilities handler = DataHandlingUtilities.getInstance();
		handler.clearJobs();
		
		//Fetch jobs from CIPRES
		Collection<CiJob> jobs = myClient.listJobs();
		
		return jobs;
	}
	
	public static JSONArray updateList(CiClient myClient, UserModel user) throws ParseException{
		
		//Create a handler instance
		DataHandlingUtilities handler = DataHandlingUtilities.getInstance();
				
		try {
			CipresUtilities.listJobs(myClient, user);
		} catch (CiCipresException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
//        handler.saveData("jobs.json", handler.getUserJSON());
//        JSONObject retObj = handler.loadData("jobs.json");
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(handler.getUserJSON());
        
        JSONArray retJSONArray = handler.getJobs((JSONObject)obj);
    	
        return retJSONArray;
        
	}
	
	public static void deleteJobs(List<String> selected_jobs,
			Collection<CiJob> allJobs) throws CiCipresException
	{
		for(int i = 0; i < selected_jobs.size(); i++){
			for(int x = 0; x < allJobs.size(); x++){
				CiJob job = (CiJob) allJobs.toArray()[x];
				System.out.println("local: " + selected_jobs.get(i));
				System.out.println("cloud" + job.getClientJobName());
				if(selected_jobs.get(i).equals(job.getClientJobName())){
					job.delete();
					selected_jobs.remove(i);
					System.out.println(job.getClientJobName() + " deleted");
				}
			}
		}
	}
	
	public static void submitJob(
			Map<String,Collection<String>> vParams, Map<String,String> inputParams, 
			Map<String,String> metadata) throws CiCipresException{

		System.err.println("SubmitJob enter");
		DataHandlingUtilities handler = DataHandlingUtilities.getInstance();
		CiClient myClient = handler.getClient();
		CiJob jobStatus = null;
		if(CipresUtilities.clientCheck(myClient) == true){
			jobStatus = myClient.submitJob("MRBAYES_XSEDE", vParams, inputParams, metadata);
		} else{
			System.err.println("No Client Found");
		}
		
		
		jobStatus.show(true);
	} 
	

}

