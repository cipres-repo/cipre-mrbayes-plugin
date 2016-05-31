package com.cipres.mrBayesPlugin.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.ngbw.directclient.CiClient;

import com.cipres.mrBayesPlugin.models.UserModel;
import com.cipres.mrBayesPlugin.models.UserModel.Job;


public class DataHandlingUtilities {
	
	private static DataHandlingUtilities singletonInstance = null;
	private List<Job> jobs = new ArrayList<Job>();
	private UserModel user = new UserModel();
	
	public static synchronized DataHandlingUtilities getInstance(){
		if(singletonInstance == null)
			singletonInstance = new DataHandlingUtilities();
		return singletonInstance;
	}
	
	public void addJob(Job job){
		synchronized (job){
			jobs.add(job);
		}
	}
	
	public List<Job> getJobs(){
		return jobs;
	}
	
	public void clearJobs(){
		jobs.clear();
	}
	
	public void addUser(UserModel user){
		synchronized (user){
			this.user = user;
		}
	}
	
	public UserModel getUser(){
		return user;
	}
	
	public CiClient getClient(){
		CiClient client = new CiClient(user.getAppKey(), user.getUsername(),
				user.getPassword(), user.getRestUrl());
		return client;
	}
	
	@SuppressWarnings("unchecked")
	public String getUserJSON(){
		
		JSONObject obj = new JSONObject();
		obj.put("username", user.getUsername());
		obj.put("password", user.getPassword());
		obj.put("restUrl", user.getRestUrl());
		obj.put("appName", user.getAppName());
		obj.put("appKey", user.getAppKey());
		
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 
		JSONArray jobJson = new JSONArray();

		for (Job job : jobs) {
			Map<String, String> jobData = new HashMap<String, String>();
			jobData.put("jobName", job.getJobName());
			jobData.put("date", df.format(job.getDate()));
			jobData.put("jobStage", job.getJobStage() + "");
			jobData.put("url", job.getUrl());
			
			jobJson.add(jobData);
		}
		obj.put("jobs", jobJson);
		return obj.toString();
	}
	
	public JSONArray getJobs(JSONObject obj){
		JSONArray jobs = (JSONArray) obj.get("jobs");
		return jobs;
	}
	
	
	
	
}
