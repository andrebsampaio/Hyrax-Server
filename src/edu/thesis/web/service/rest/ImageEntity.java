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
	@Basic private String photoPath;
	
	public ImageEntity(String photoPath){
		this.photoPath = photoPath;
	}
	
	public ImageEntity(){
		
	}
	
	public String getPath(){
		return photoPath;
	}
	
	public void setPath(String path){
		this.photoPath = path;
	}
	
	public int getId(){
		return this.id;
	}
	
	
}
