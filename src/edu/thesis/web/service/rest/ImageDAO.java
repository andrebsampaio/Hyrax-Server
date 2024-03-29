package edu.thesis.web.service.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	List<DeviceDAO> devices;
	
	public void setDevices(List<DeviceDAO> devices) {
		this.devices = devices;
	}

	public List<DeviceDAO> getDevices() {
		return devices;
	}

	String photoName;
	
	public ImageDAO(int id, String location, String time, List <DeviceDAO> devices){
		this.id = id;
		this.location = location;
		this.time = time;
		this.devices = devices;
		this.photoName = location + time;
	}
	
	public ImageDAO(){
		devices = new ArrayList<>();
	}
	
	public void setPhotoName(String photoName){
		this.photoName = photoName;
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
