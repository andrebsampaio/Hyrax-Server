package edu.thesis.web.service.rest;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class User {
	
	@Id @GeneratedValue 
	private int id;
	
	@Basic private String eigenfacePath;
	@Basic private String name;
	@ManyToMany(targetEntity=ImageEntity.class)
	private Set ImageEntity;
	
	public User (){}
	
	public User (String name, String eigenfacePath){
		this.setName(name);
		this.setEigenfacePath(eigenfacePath);
	}

	public String getEigenfacePath() {
		return eigenfacePath;
	}

	public void setEigenfacePath(String eigenfacePath) {
		this.eigenfacePath = eigenfacePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
