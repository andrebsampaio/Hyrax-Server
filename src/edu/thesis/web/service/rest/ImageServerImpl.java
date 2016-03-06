package edu.thesis.web.service.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
 
@Path("/")
public class ImageServerImpl implements ImageServer {

	@GET
	@Path("/images/{imageId}")
	@Produces("image/jpg")
	public Response downloadImage(@PathParam("imageId") String imageId) {
	    ResponseBuilder response = Response.ok((Object) new File("/home/abs/Pictures/pic.jpg"));
	    response.header("Content-Disposition",
	            "attachment; filename=\"pic.jpg\"");
	    return response.build();
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadImage(
		@FormDataParam("image") InputStream uploadedInputStream,
		@FormDataParam("details") String imageDetail) {
		
		ObjectMapper mapper = new ObjectMapper();
		ImageDetails obj = null;
		try {
			obj = mapper.readValue(imageDetail, ImageDetails.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String uploadedFileLocation = "/home/abs/hyraxWorkspace/hyrax-server/images/" + obj.getLocation() + obj.getTime();

		// save it
		writeToFile(uploadedInputStream, uploadedFileLocation);

		String output = "File uploaded to : " + uploadedFileLocation;

		return Response.status(200).entity(output).build();

	}

	// save uploaded file to new location
	private void writeToFile(InputStream uploadedInputStream,
		String uploadedFileLocation) {

		try {
			OutputStream out = new FileOutputStream(new File(
					uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
}
