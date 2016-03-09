package edu.thesis.web.service.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
import com.sun.jersey.multipart.FormDataParam;

import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.queryframework.ReadAllQuery;
import oracle.toplink.essentials.queryframework.ReadObjectQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;
 
@Path("/")
public class ImageServerImpl implements ImageServer {
	
	private EntityManagerFactory emf;
	private EntityManager em;
	private String PERSISTENCE_UNIT_NAME = "hyrax-server";
	
	public ImageServerImpl(){
		initEntityManager();
	}
	
	private void initEntityManager() {
		 emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		 em = emf.createEntityManager();
		
	}
	
	@GET
	@Path("/images")
	@Produces({MediaType.APPLICATION_JSON})
	public List<ImageDAO> getAllIds(){
		List<ImageEntity> images = em.createQuery("select i from ImageEntity i").getResultList();
		List<ImageDAO> dao = new ArrayList<ImageDAO>();
		for (ImageEntity i : images){
			dao.add(new ImageDAO(i.getId(),i.getLocation(),i.getTime()));
		}
		return dao;
	}
	
	@GET
	@Path("/images/{imageId}")
	@Produces("image/jpg")
	public Response downloadImage(@PathParam("imageId") String imageId) {
		ImageEntity img = (ImageEntity) em.createQuery("select i from ImageEntity i where i.id = :imageId ")
				.setParameter("imageId", Integer.parseInt(imageId))
				.getSingleResult();
		System.out.println(img.getPath());
		File f = new File(img.getPath());
	    ResponseBuilder response = Response.ok((Object) f);
	    response.header("Content-Disposition",
	            "attachment; filename=" + f.getName());
	    return response.build();
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadImage(
		@FormDataParam("image") InputStream uploadedInputStream,
		@FormDataParam("details") String imageDetail) {
		
		System.out.println(imageDetail);
		
		ObjectMapper mapper = new ObjectMapper();
		ImageEntity obj = null;
		try {
			obj = mapper.readValue(imageDetail, ImageEntity.class);
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
		
		File imgDir = new File ("HyraxImages/");
		if (!imgDir.exists()){
			imgDir.mkdir();
		}
		
		String id = obj.getLocation() + obj.getTime();
		
		String uploadedFileLocation =  imgDir.getAbsolutePath() + "/" + id + ".jpg";
		obj.setPath(uploadedFileLocation);
		
		// save it
		writeToFile(id, uploadedInputStream, uploadedFileLocation);

		em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();
		
		String output = "File uploaded to : " + uploadedFileLocation;
		
		return Response.status(200).entity(output).build();

	}
	
	// save uploaded file to new location
	private void writeToFile(String id, InputStream uploadedInputStream,
		String uploadedFileLocation) {

		try {
			
			int read = 0;
			byte[] bytes = new byte[1024];
			
			File img = new File(uploadedFileLocation);
			OutputStream out = new FileOutputStream(img);
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
