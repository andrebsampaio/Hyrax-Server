package edu.thesis.web.service.rest;

import java.util.Date;

public class ImageDetails {
	
	String id;
	String location;
	String time;
	
	public ImageDetails(String id, String location, String time){
		this.id = id;
		this.location = location;
		this.time = time;
	}
	
	public ImageDetails(){
		
	}
	
	public String getId(){
		return this.id;
	}
	
	public String getLocation(){
		return this.location;
	}
	
	public String getTime(){
		return this.time;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setLocation(String location){
		this.location = location;
	}
	
	public void setTime(String time){
		this.time = time;
	}
	
	
}
