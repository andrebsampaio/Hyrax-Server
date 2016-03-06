package edu.thesis.web.service.rest;

import java.io.InputStream;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@WebService
public interface ImageServer {
	
	//download a image from server
	@WebMethod Response downloadImage(String imageId);
		
	//update image to server
	@WebMethod Response uploadImage(InputStream uploadedInputStream,
			String imageDetail);
}
