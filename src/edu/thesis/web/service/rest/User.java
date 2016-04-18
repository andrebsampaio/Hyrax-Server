package edu.thesis.web.service.rest;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="PERSON")
public class User {
	
	@Id @GeneratedValue 
	private int id;
	
	public int getId() {
		return id;
	}

	@Basic
	@Column(name = "NAME", length = 20) 
	private String name;
	@ManyToMany(targetEntity=ImageEntity.class)
	private Set<ImageEntity> images;
	
	public User (){}
	
	public User (String name){
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addImage(ImageEntity image){
		images.add(image);
	}
}
