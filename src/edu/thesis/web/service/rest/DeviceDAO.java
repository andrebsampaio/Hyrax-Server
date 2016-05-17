package edu.thesis.web.service.rest;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceDAO implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String deviceWD;
	public DeviceDAO(String deviceWD, String deviceBT) {
		this.deviceWD = deviceWD;
		this.deviceBT = deviceBT;
	}
	
	public DeviceDAO(){}
	
	public String getDeviceWD() {
		return deviceWD;
	}
	public void setDeviceWD(String deviceWD) {
		this.deviceWD = deviceWD;
	}
	public String getDeviceBT() {
		return deviceBT;
	}
	public void setDeviceBT(String deviceBT) {
		this.deviceBT = deviceBT;
	}
	String deviceBT;
	
	
	
}
