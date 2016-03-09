package edu.thesis.web.service.rest;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
public class ImageEntity {
	
	@Id @GeneratedValue 
	private int id;
	@Basic private String location;
	@Basic private String time;
	@Basic private String path;
	
	public ImageEntity(String location, String time, String path){
		this.location = location;
		this.time = time;
		this.path = path;
	}
	
	public ImageEntity(){
		
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
	
	public String getPath() {
		return this.path;
	}
	
	public String setPath(String path){
		return this.path = path;
	}
	
	
}
