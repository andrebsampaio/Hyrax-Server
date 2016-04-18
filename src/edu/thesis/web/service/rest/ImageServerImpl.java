package edu.thesis.web.service.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.ml.annotation.ScoredAnnotation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import org.json.JSONArray;

@Path("/rest")
public class ImageServerImpl implements ImageServer {

	private EntityManagerFactory emf;
	private EntityManager em;
	private String PERSISTENCE_UNIT_NAME = "hyrax-server";

	@Context
	private ServletContext context;

	public ImageServerImpl(){
		initEntityManager();
	}

	private void initEntityManager() {
		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		em = emf.createEntityManager();
	}

	@GET
	@Path("/test")
	@Produces({ MediaType.APPLICATION_JSON})
	public String testDetection(){
		GroupedDataset g = FaceProcessingUtils.getGroupedDataset(new File("/home/abs/trainpaio/"));
		em.getTransaction().begin();
		for (Object name : g.keySet()){
			String aux = String.valueOf(name);
			System.out.println(aux);
			User u = new User(aux.toLowerCase());
			em.persist(u);
		}
		em.getTransaction().commit();
		List<Integer> users = em.createQuery("select i.id from User i").getResultList();
		return new JSONArray(users).toString();
	}

	@POST
	@Path("/search")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public List<ImageDAO> searchImage(FormDataMultiPart formParams){
		Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();
		String name = "";
		for (List<FormDataBodyPart> fields : fieldsByName.values())
		{
			for (FormDataBodyPart field : fields)
			{
				if (field.getName().equals("person_name")){
					name = field.getEntityAs(String.class);
				}
			}
		}

		List<ImageEntity> images = em.createQuery("SELECT i FROM ImageEntity i JOIN i.users u WHERE u.name = :name")
				.setParameter("name", name)
				.getResultList();
		List<ImageDAO> dao = new ArrayList<ImageDAO>();
		for (ImageEntity i : images){
			dao.add(new ImageDAO(i.getId(),i.getLocation(),i.getTime()));
		}
		return dao;
	}

	@GET
	@Path("/images")
	@Produces({MediaType.APPLICATION_JSON})
	public List<ImageDAO> getAllIds(){
		List<ImageEntity> images = em.createQuery("select i from ImageEntity i").getResultList();
		System.out.println(images.size());
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
	public Response uploadImage(FormDataMultiPart formParams)
	{
		Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();
		ImageEntity obj = null;
		File imageFile = null;
		for (List<FormDataBodyPart> fields : fieldsByName.values())
		{
			for (FormDataBodyPart field : fields)
			{
				if (field.getName().equals("details")){
					ObjectMapper mapper = new ObjectMapper();
					String details = field.getEntityAs(String.class);

					try {
						obj = mapper.readValue(details, ImageEntity.class);
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();	        		} 
					catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if (field.getName().equals("image")) {
					InputStream is = field.getEntityAs(InputStream.class);
					String fileName = field.getContentDisposition().getFileName();
					String folderName = fileName.split("\\.")[0];

					File imgDir = new File ("HyraxImages/");
					if (!imgDir.exists()){
						imgDir.mkdir();
					}

					String imageLocation =  imgDir.getAbsolutePath() + "/" + folderName + "/" + fileName ;

					imageFile = new File(imageLocation);

					if (!imageFile.getParentFile().exists()){
						imageFile.getParentFile().mkdirs();
					}

					// save it
					writeToFile(is, imageLocation);		
				}
			}
		}
		System.out.println(imageFile.getAbsolutePath());
		FaceRecognitionEngine<KEDetectedFace, String> engine = (FaceRecognitionEngine<KEDetectedFace, String>) context.getAttribute("engine");
		List<ScoredAnnotation<String>> result = FaceProcessingUtils.recognizeFacesRevised(imageFile, engine);
		
		for (ScoredAnnotation<String> a : result){
			System.out.println(a.annotation.toLowerCase());
			final String qString = "select u from User u where u.name = :name";
			TypedQuery<User> query = em.createQuery(qString,User.class);
			query.setParameter("name", a.annotation.toLowerCase());
			User user = query.getSingleResult();
			obj.addUser(user);
			user.addImage(obj);
		}

		obj.setPath(imageFile.getAbsolutePath());

		em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();

		String output = "File uploaded to successfuly";

		return Response.status(200).entity(output).build();

	}

	/*@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadImage(FormDataMultiPart formParams)
	{
	    Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();

	    File imgDir = new File ("HyraxImages/");
		if (!imgDir.exists()){
			imgDir.mkdir();
		}

		String folderName = "";
		File imageFile = null;
		ImageEntity obj = null;
		Set<Face> faces = new HashSet<Face>(); 

	    for (List<FormDataBodyPart> fields : fieldsByName.values())
	    {
	        for (FormDataBodyPart field : fields)
	        {
	        	System.out.println(field.getName());

	        	if (field.getName().equals("details")){
	        		ObjectMapper mapper = new ObjectMapper();
	        		String details = field.getEntityAs(String.class);

	        		try {
	        			obj = mapper.readValue(details, ImageEntity.class);
	        		} catch (JsonParseException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();	        		} catch (JsonMappingException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		} catch (IOException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}

	        	} else if (field.getName().equals("image")) {
	        		InputStream is = field.getEntityAs(InputStream.class);
	        		String fileName = field.getContentDisposition().getFileName();
	        		if (folderName == ""){
	        			folderName = fileName.split("\\.")[0];
	        		}

		            String imageLocation =  imgDir.getAbsolutePath() + "/" + folderName + "/" + fileName ;

		            imageFile = new File(imageLocation);

		            if (!imageFile.getParentFile().exists()){
		            	imageFile.getParentFile().mkdirs();
		            }

		            // save it
		    		writeToFile(is, imageLocation);	

	        	} else if (field.getName().equals("face")){
	        		InputStream is = field.getEntityAs(InputStream.class);
	        		String [] info = field.getContentDisposition().getFileName().split("_");
	        		if (folderName == ""){
	        			folderName = info[0];
	        		}

		            String faceName = info[1];


	        		File faceFile = new File (imgDir.getAbsolutePath() + "/" + folderName + "/faces/" + faceName);

	                faceFile.getParentFile().mkdirs();

		            // save it
		    		writeToFile(is, faceFile.getAbsolutePath());

		    		Face face = new Face();
		    		face.setPath(faceFile.getAbsolutePath());
		    		faces.add(face);
	        	}

	        }
	    }

	    for(Face f : faces){
    		f.setI(obj);
    	}

    	obj.setFaces(faces);
    	obj.setPath(imageFile.getAbsolutePath());

    	em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();

	    String output = "File uploaded to successfuly";

		return Response.status(200).entity(output).build();
	}*/

	// save uploaded file to new location
	private void writeToFile(InputStream uploadedInputStream,
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

	/*private List<ImageEntity> recognizeInFaces(FaceRecognizer eigenFace, List<ImageEntity> images, Size size){
		List<ImageEntity> detectedImages = new ArrayList<ImageEntity>();
		double[] prediction = new double[1];
		int[] predictionImageLabel = new int[1]; 
		System.out.println("Number of images: " + images.size());
		for (ImageEntity i : images){
			for (Face f : i.getFaces()){
				System.out.println("Number of faces: " + i.getFaces().size());
				Mat face = processFace(f,size);
				eigenFace.predict(face, predictionImageLabel, prediction);
				if (prediction[0] < 5000000){
					System.out.println("HELLOOOOO - " + prediction[0]);
					detectedImages.add(i);
					break;
				}
			}

		}
		return detectedImages;
	}

	private Mat processFace(Face f, Size size) {
		Mat image = Imgcodecs.imread(f.getPath(), org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		Imgproc.resize(image, image, size);
		return image;
	}*/
}
