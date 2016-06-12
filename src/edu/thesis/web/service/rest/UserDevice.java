package edu.thesis.web.service.rest;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class UserDevice {
	
	@EmbeddedId private DeviceId id;
	
	@ManyToMany(targetEntity=ImageEntity.class, mappedBy= "devices")
	private Set<ImageEntity> images;

	public UserDevice(String deviceWD, String deviceBT) {
		id = new DeviceId();
		id.deviceBT = deviceBT.toUpperCase();
		id.deviceWD = deviceWD.toUpperCase();
		this.images = new HashSet<>();
	}
	
	public UserDevice(){
		this.images = new HashSet<>();
	}
	
	public void addImage(ImageEntity i){
		images.add(i);
	}
	
	public void removeImage(ImageEntity i){
		images.remove(i);
	}
	
	public Set<ImageEntity> getImages() {
		return images;
	}

	public void setImages(Set<ImageEntity> images) {
		this.images = images;
	}
	
	public String getDeviceWD() {
		return id.deviceWD;
	}

	public void setDeviceWD(String deviceWD) {
		id.deviceWD = deviceWD;
	}

	public String getDeviceBT() {
		return id.deviceBT;
	}

	public void setDeviceBT(String deviceBT) {
		id.deviceBT = id.deviceBT;
	}
	
}

@Embeddable
class DeviceId {
    String deviceWD;
    String deviceBT;
    
    
}


