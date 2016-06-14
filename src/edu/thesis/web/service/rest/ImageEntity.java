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
	@Basic private String path;
	@Basic private int numberOfPeople;
	@ManyToMany(targetEntity=User.class)
	private Set<User> users;
	
	public ImageEntity(String location, String time, String path){
		this.location = location;
		this.time = time;
		this.path = path;
		users = new HashSet<User>();
	}
	
	
	public Set<User> getUsers(){
		return users;
	}
	
	public void untagUser(User u){
		users.remove(u);
	}

	public ImageEntity(){
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
	
	public String getPath() {
		return this.path;
	}
	
	public String setPath(String path){
		return this.path = path;
	}
	
	
}
