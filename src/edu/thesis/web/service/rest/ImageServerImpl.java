package edu.thesis.web.service.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.helper.opencv_core;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
 
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
	
	@POST
	@Path("/search")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public List<ImageDAO> searchImage(FormDataMultiPart formParams){
		Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();
		FaceRecognizer faceRecognizer = null;
		List<ImageEntity> detectedImages;
		Size size;
		int width = 0;int height = 0;
		for (List<FormDataBodyPart> fields : fieldsByName.values())
	    {
	        for (FormDataBodyPart field : fields)
	        {
	        	if (field.getName().equals("eigenface")){
	        		File imgDir = new File ("HyraxEigenfaces/");
	        		if (!imgDir.exists()){
	        			imgDir.mkdir();
	        		}
	        		
	        		InputStream is = field.getEntityAs(InputStream.class);
		            String fileName = field.getName();

		            String uploadedFileLocation =  imgDir.getAbsolutePath() + "/" + fileName + ".xml";
		            
		            // save it
		    		writeToFile(is, uploadedFileLocation);
	        		
	        		faceRecognizer = opencv_face.createEigenFaceRecognizer();
	        		faceRecognizer.load(uploadedFileLocation);
	        		
	        	} else {
	        		if (field.getName().equals("train_width")){
	        			width = Integer.parseInt(field.getEntityAs(String.class));
	        		} else if (field.getName().equals("train_height")) {
	        			height = Integer.parseInt(field.getEntityAs(String.class));
	        		}
	        	}
	        }
	    }
		
		List<ImageEntity> images = em.createQuery("select i from ImageEntity i").getResultList();
		detectedImages = this.recognizeInFaces(faceRecognizer, images, new Size(width,height));
		
		List<ImageDAO> responseImages = new ArrayList<>();
		
		for (ImageEntity i : detectedImages){
			responseImages.add(new ImageDAO(i.getId(),i.getLocation(),i.getTime()));
		}
		return responseImages;
		
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
	public Response uploadImage(FormDataMultiPart formParams)
	{
	    Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();
	    
	    File imgDir = new File ("HyraxImages/");
		if (!imgDir.exists()){
			imgDir.mkdir();
		}
		
		String fileName = "";
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
	        			e.printStackTrace();
	        		} catch (JsonMappingException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		} catch (IOException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}
	        		
	        	} else if (field.getName().equals("image")) {
	        		InputStream is = field.getEntityAs(InputStream.class);
	        		if (fileName == ""){
	        			fileName = field.getContentDisposition().getFileName();
	        		}
	        		System.out.println(fileName);
		            String imageLocation =  imgDir.getAbsolutePath() + "/" + fileName.split("\\.")[0] + "/" + fileName;
		            
		            imageFile = new File(imageLocation);
		            
	                imageFile.getParentFile().mkdirs();
	         
		            // save it
		    		writeToFile(is, imageLocation);	
		    		
	        	} else if (field.getName().equals("face")){
	        		InputStream is = field.getEntityAs(InputStream.class);
	        		String [] info = field.getContentDisposition().getFileName().split("_");
	        		if (fileName == ""){
	        			fileName = info[0];
	        		}
		            String faceName = info[1];
		        
		            
	        		
	        		File faceFile = new File (imgDir.getAbsolutePath() + "/" + fileName + "/faces/" + faceName);
		            
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
	}
	
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
	
	private List<ImageEntity> recognizeInFaces(FaceRecognizer eigenFace, List<ImageEntity> images, Size size){
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
		 Mat image = opencv_imgcodecs.imread(f.getPath(), opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
	     opencv_imgproc.resize(image, image, size);
		 return image;
	}
}
