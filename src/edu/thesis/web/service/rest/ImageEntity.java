package edu.thesis.web.service.rest;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
public class ImageEntity {
	
	@Id @GeneratedValue 
	private int id;
	@Basic private String location;
	@Basic private String time;
	@ManyToMany(targetEntity=UserDevice.class, cascade = {CascadeType.MERGE})
	private Set<UserDevice> devices;
	

	@Basic private String photoName; 
	@Basic private int numberOfPeople;
	@Basic private String photoPath;
	@ManyToMany(targetEntity=User.class)
	private Set<User> users;
	
	public ImageEntity(String location, String time, String deviceWD, String deviceBT, String photoName, String photoPath){
		this.location = location;
		this.photoName = photoName;
		this.time = time;
		this.devices = new HashSet<UserDevice>();
		devices.add(new UserDevice(deviceWD, deviceBT));
		this.photoPath = photoPath;
		users = new HashSet<User>();
	}
	
	
	public Set<UserDevice> getDevices() {
		return devices;
	}


	public void setDevices(Set<UserDevice> devices) {
		this.devices = devices;
	}
	
	public void addDevice(UserDevice device){
		devices.add(device);
	}
	
	public void removeDevice(UserDevice device){
		devices.remove(device);
	}
	
	public String getPath(){
		return photoPath;
	}
	
	public void setPath(String path){
		this.photoPath = path;
	}
	
	public String getPhotoName(){
		return photoName;
	}
	
	public void setPhotoName(String photoName){
		this.photoName = photoName;
	}
	
	public Set<User> getUsers(){
		return users;
	}
	
	public void untagUser(User u){
		users.remove(u);
	}

	public ImageEntity(){
		devices= new HashSet<UserDevice>();
		users = new HashSet<User>();
	}
	
	public void addUser(User user){
		users.add(user);
	}
	
	public int getId(){
		return this.id;
	}
	
	public int getNumberOfPeople(){
		return numberOfPeople;
	}
	
	public void setNumberOfPeople(int number){
		numberOfPeople = number;
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
