package edu.thesis.web.service.rest;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.core.Response;

import com.sun.jersey.multipart.FormDataMultiPart;

@WebService
public interface ImageServer {
	
	//download a image from server
	@WebMethod Response downloadImage(String imageId);
		
	//update image to server
	@WebMethod Response uploadImage(FormDataMultiPart formParams);
}
