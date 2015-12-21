package com.cipres.mrBayesPlugin.models;

import java.util.Date;
import java.util.List;

public class UserModel {
	public String username;
	public String password;
	public String restUrl;
	public String appKey;
	public String appName;
	
	public List<Job> jobs;
	
	public UserModel(){
		this.username = null;
		this.password = null;
		this.restUrl = null;
		this.appKey = null;
		this.appName = null;
	}
	
	public UserModel(String username, String password, String restUrl, String appKey, String appName){
		this.username = username;
		this.password = password;
		this.restUrl = restUrl;
		this.appKey = appKey;
		this.appName = appName;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRestUrl() {
		return restUrl;
	}

	public void setRestUrl(String restUrl) {
		this.restUrl = restUrl;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appname) {
		this.appName = appname;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	public class Job{
		public String jobName;
		public Date date;
		public String jobStage;
		
		public String getJobName() {
			return jobName;
		}
		public void setJobName(String jobName) {
			this.jobName = jobName;
		}
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		public String getJobStage() {
			return jobStage;
		}
		public void setJobStage(String jobStage) {
			this.jobStage = jobStage;
		}
	}
}
