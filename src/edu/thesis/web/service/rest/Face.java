package edu.thesis.web.service.rest;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Face {
	
	@Id @GeneratedValue
	private int id;
	
	@Basic private String path;
	
	@ManyToOne(targetEntity=ImageEntity.class)
	ImageEntity i;
	
	public Face (String path, ImageEntity i){
		this.path = path;
		this.i = i;
	}
	
	public Face(){}
	
	public ImageEntity getI() {
		return i;
	}

	public void setI(ImageEntity i) {
		this.i = i;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
