package edu.thesis.web.service.rest;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ImageDAO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int id;
	String location;
	String time;
	
	public ImageDAO(int id, String location, String time){
		this.id = id;
		this.location = location;
		this.time = time;
	}
	
	public ImageDAO(){
		
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	public String getLocation(){
		return this.location;
	}
	
	public String getTime(){
		return this.time;
	}
	
	
	public void setLocation(String location){
		this.location = location;
	}
	
	public void setTime(String time){
		this.time = time;
	}
	
	
}