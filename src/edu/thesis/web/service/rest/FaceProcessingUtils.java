package edu.thesis.web.service.rest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.evaluation.retrieval.Scored;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.image.model.EigenImages;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.RotateScaleAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.recognition.AnnotatorFaceRecogniser;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.processing.face.recognition.FisherFaceRecogniser;
import org.openimaj.image.processing.face.similarity.FaceSimilarityEngine;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;

public class FaceProcessingUtils {
	
	private static final String HOME_PATH = System.getProperty("user.home");
	
	public static void findFaces(File file, String cascadePath){
		try {
			FileInputStream input = new FileInputStream(file);
			MBFImage image = ImageUtilities.readMBF(input);
			BufferedImage detectedFacesImage = ImageIO.read(file);
			File cascadeFolder = new File (cascadePath);
			FaceDetector<DetectedFace, FImage> haar = new HaarCascadeDetector(cascadeFolder.listFiles()[2].getAbsolutePath());
			FaceDetector<KEDetectedFace,FImage> fd = new FKEFaceDetector(haar);
			List<KEDetectedFace> faces = fd.detectFaces( Transforms.calculateIntensity( image ) );
			System.out.println(faces.size());

			System.out.println("# Found faces, one per line.");
			System.out.println("# <x>, <y>, <width>, <height>");
			Iterator<KEDetectedFace> iterator = faces.iterator(); 
			RotateScaleAligner aligner = new RotateScaleAligner();
			int i = 0;
			while (iterator.hasNext()) {
				KEDetectedFace face = iterator.next();
				Rectangle bounds = face.getBounds();
				System.out.println(bounds.x + ", " + bounds.y + ", " + bounds.width + ", " + bounds.height);

				BufferedImage aux = ImageUtilities.createBufferedImage(aligner.align(face));
				//saveFace(aux, file.getParentFile(), "face" + i );
				i++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static GroupedDataset getGroupedDataset(File folder){
		try {

			VFSGroupDataset<FImage> groupedFaces =  
					new VFSGroupDataset<FImage>(HOME_PATH + File.separator + "trainpaio", ImageUtilities.FIMAGE_READER);
			System.out.println("train images size: " + groupedFaces.size());
			return groupedFaces;
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void machineLearningStuff(){
		GroupedDataset<String, VFSListDataset<FImage>, FImage> allData = null;
		try {
			allData = new VFSGroupDataset<FImage>("/home/abs/101_ObjectCategories", ImageUtilities.FIMAGE_READER);
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GroupedDataset<String, ListDataset<FImage>, FImage> data = 
				GroupSampler.sample(allData, 5, false);

		GroupedRandomSplitter<String, FImage> splits = 
				new GroupedRandomSplitter<String, FImage>(data, 15, 0, 15);

		DenseSIFT dsift = new DenseSIFT(5, 7);
		PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 7);
		
		System.out.println(allData.size());
		HardAssigner<byte[], float[], IntFloatPair> assigner = 
				trainQuantiser(data, pdsift);
		System.out.println("Quantitised");

		FeatureExtractor<DoubleFV, FImage> extractor = new PHOWExtractor(pdsift, assigner);

		LiblinearAnnotator<FImage, String> ann = new LiblinearAnnotator<FImage, String>(
				extractor, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
		System.out.println(allData.size());
		ann.train((GroupedDataset) allData);
		System.out.println("train complete");
		
		String [] files = {"chair", "camera", "elephant"};
		
		try {
			for (String name : files){
				List<ScoredAnnotation<String>> result = ann.annotate(ImageUtilities.readF(new File("/home/abs/Pictures/" + name + ".jpg")));
				for (ScoredAnnotation<String> sa : result){
					System.out.println("I think it is a " + sa.annotation + " with " + sa.confidence);
				}
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static class PHOWExtractor implements FeatureExtractor<DoubleFV, FImage> {
		PyramidDenseSIFT<FImage> pdsift;
		HardAssigner<byte[], float[], IntFloatPair> assigner;

		public PHOWExtractor(PyramidDenseSIFT<FImage> pdsift, HardAssigner<byte[], float[], IntFloatPair> assigner)
		{
			this.pdsift = pdsift;
			this.assigner = assigner;
		}

		public DoubleFV extractFeature(FImage object) {
			FImage image = object.getImage();
			pdsift.analyseImage(image);

			BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);

			BlockSpatialAggregator<byte[], SparseIntFV> spatial = new BlockSpatialAggregator<byte[], SparseIntFV>(
					bovw, 2, 2);

			return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
		}
	}



	static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(
			Dataset<FImage> sample, PyramidDenseSIFT<FImage> pdsift)
	{
		List<LocalFeatureList<ByteDSIFTKeypoint>> allkeys = new ArrayList<LocalFeatureList<ByteDSIFTKeypoint>>();

		for (FImage rec : sample) {
			FImage img = rec;

			pdsift.analyseImage(img);
			allkeys.add(pdsift.getByteKeypoints(0.005f));
		}

		if (allkeys.size() > 10000)
			allkeys = allkeys.subList(0, 10000);

		ByteKMeans km = ByteKMeans.createKDTreeEnsemble(300);
		DataSource<byte[]> datasource = new LocalFeatureListDataSource<ByteDSIFTKeypoint, byte[]>(allkeys);
		ByteCentroidsResult result = km.cluster(datasource);

		return result.defaultHardAssigner();
	}
	
	public static void faceSimilarity(){
		// first, we load two images
		FImage image1 = null;
		FImage image2 = null;
		try {
			final URL image1url = new URL(
					"http://s3.amazonaws.com/rapgenius/fema_-_39841_-_official_portrait_of_president-elect_barack_obama_on_jan-_13.jpg");
			final URL image2url = new URL("http://revistaquem.globo.com/Revista/Quem/foto/0,,61648518,00.jpg");
			image1 = ImageUtilities.readF(image1url);
			image2 = ImageUtilities.readF(image2url);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// then we set up a face detector; will use a haar cascade detector to
		// find faces, followed by a keypoint-enhanced detector to find facial
		// keypoints for our feature. There are many different combinations of
		// features and detectors to choose from.
		final HaarCascadeDetector detector = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
		final FKEFaceDetector kedetector = new FKEFaceDetector(detector);

		// now we construct a feature extractor - this one will extract pixel
		// patches around prominant facial keypoints (like the corners of the
		// mouth, etc) and build them into a vector.
		final org.openimaj.image.processing.face.feature.FacePatchFeature.Extractor extractor = new FacePatchFeature.Extractor();
		

		// in order to compare the features we need a comparator. In this case,
		// we'll use the Euclidean distance between the vectors:
		final FaceFVComparator<FacePatchFeature, FloatFV> comparator =
				new FaceFVComparator<FacePatchFeature, FloatFV>(FloatFVComparison.EUCLIDEAN);

		// Now we can construct the FaceSimilarityEngine. It is capable of
		// running the face detector on a pair of images, extracting the
		// features and then comparing every pair of detected faces in the two
		// images:
		final FaceSimilarityEngine<KEDetectedFace, FacePatchFeature, FImage> engine =
				new FaceSimilarityEngine<KEDetectedFace, FacePatchFeature, FImage>(kedetector, extractor, comparator);


		// we need to tell the engine to use our images:
		engine.setQuery(image1, "image1");
		engine.setTest(image2, "image2");


		// and then to do its work of detecting, extracting and comparing
		engine.performTest();

		// finally, for this example, we're going to display the "best" matching
		// faces in the two images. The following loop goes through the map of
		// each face in the first image to all the faces in the second:
		for (final Entry<String, Map<String, Double>> e : engine.getSimilarityDictionary().entrySet()) {
			// this computes the matching face in the second image with the
			// smallest distance:
			double bestScore = Double.MAX_VALUE;
			String best = null;
			for (final Entry<String, Double> matches : e.getValue().entrySet()) {
				if (matches.getValue() < bestScore) {
					bestScore = matches.getValue();
					best = matches.getKey();
				}
			}

			// and this composites the original two images together, and draws
			// the matching pair of faces:
			final FImage img = new FImage(image1.width + image2.width, Math.max(image1.height, image2.height));
			img.drawImage(image1, 0, 0);
			img.drawImage(image2, image1.width, 0);

			img.drawShape(engine.getBoundingBoxes().get(e.getKey()), 1F);

			final Rectangle r = engine.getBoundingBoxes().get(best);
			r.translate(image1.width, 0);
			img.drawShape(r, 1F);

			// and finally displays the result
			DisplayUtilities.display(img);
		}
	}

	public static void recognizeFaceManual(File f, String cascadeFolder){
		try {
			VFSGroupDataset<FImage> dataset = 
					new VFSGroupDataset<FImage>(f.getAbsolutePath(), ImageUtilities.FIMAGE_READER);

			int nTraining = 5;
			int nTesting = 5;
			GroupedRandomSplitter<String, FImage> splits = 
					new GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting);
			GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
			GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();

			List<FImage> basisImages = DatasetAdaptors.asList(training);
			int nEigenvectors = 100;
			EigenImages eigen = new EigenImages(nEigenvectors);
			eigen.train(basisImages);

			Map<String, DoubleFV[]> features = new HashMap<String, DoubleFV[]>();
			for (final String person : training.getGroups()) {
				final DoubleFV[] fvs = new DoubleFV[nTraining];

				for (int i = 0; i < nTraining; i++) {
					final FImage face = training.get(person).get(i);
					fvs[i] = eigen.extractFeature(face);
				}
				features.put(person, fvs);
			}

			double correct = 0, incorrect = 0;
			for (String truePerson : testing.getGroups()) {
				for (FImage face : testing.get(truePerson)) {
					DoubleFV testFeature = eigen.extractFeature(face);

					String bestPerson = null;
					double minDistance = Double.MAX_VALUE;
					for (final String person : features.keySet()) {
						for (final DoubleFV fv : features.get(person)) {
							double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);

							if (distance < minDistance) {
								minDistance = distance;
								bestPerson = person;
							}
						}
					}

					System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);

					if (truePerson.equals(bestPerson))
						correct++;
					else
						incorrect++;
				}
			}

			System.out.println("Accuracy: " + (correct / (correct + incorrect)));
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}	

	public FaceRecognitionEngine<KEDetectedFace, String> loadEngine (File engineFile){
		try {
			return FaceRecognitionEngine.load(engineFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<ScoredAnnotation<String>> recognizeFacesRevised(File toInspect, FaceRecognitionEngine<KEDetectedFace, String> engine){
		
		List<ScoredAnnotation<String>> detectedPeople = new ArrayList<ScoredAnnotation<String>>();
		
		try{
			List<IndependentPair<KEDetectedFace, ScoredAnnotation<String>>> results = engine.recogniseBest(ImageUtilities.readF(toInspect));
			System.out.println("Number of people " + results.size());
			int fCount = 0;
			for (IndependentPair<KEDetectedFace, ScoredAnnotation<String>> pair: results) {
				KEDetectedFace face = pair.firstObject();
				ScoredAnnotation<String> annotation = pair.secondObject();
				saveFace((DetectedFace)face, toInspect.getParentFile(), String.valueOf(fCount++));
				detectedPeople.add(annotation);
			}
			
			
		} catch (IOException e){
			e.printStackTrace();
		}
		
		return detectedPeople;
		
	}
	
	
//	public static boolean recognizeFaces(File f, GroupedDataset dataset, String cascadeFolder){
//		FaceDetector<DetectedFace, FImage> haar = new HaarCascadeDetector(new File(cascadeFolder).listFiles()[2].getAbsolutePath());
//		FKEFaceDetector faceDetector = new FKEFaceDetector(haar);
//		//EigenFaceRecogniser<KEDetectedFace, String> faceRecognizer = EigenFaceRecogniser.create(18, new RotateScaleAligner(), 1, DoubleFVComparison.CORRELATION, 0.9f); 
//		//FaceRecognitionEngine<KEDetectedFace, String> engine = FaceRecognitionEngine.create(faceDetector, faceRecognizer);
//		//engine.train(dataset);
//		int detected = 0;
//		KEDetectedFace face = null;
//		try {
//			FImage image = ImageUtilities.readF(f);
//			for (int i = 0; i < 1; i++){
//
//				FaceRecognitionEngine<KEDetectedFace, String> engine = createAndTrainRecognitionEngine(dataset, faceDetector, new RotateScaleAligner(), 20, 0.7f, 1);
//				if (detected == 2 || (i == 4 && detected == 0)){break;}
//
//				List<IndependentPair<KEDetectedFace, ScoredAnnotation<String>>> results = engine.recogniseBest(image);
//				if (i == 1) System.out.println("possible people size " + results.size());
//				System.out.println("Positive: " + detected);
//				System.out.println("Probe: " + i);
//				System.out.println("");
//				int x = 0;
//				for (IndependentPair<KEDetectedFace, ScoredAnnotation<String>> pair: results) {
//					KEDetectedFace aux = pair.firstObject();
//					ScoredAnnotation<String> annotation = pair.secondObject();
//					if (annotation != null){
//						if (x == 0) detected++;
//						x++;
//						if (face == null){
//							face = aux;
//							//saveFace(ImageUtilities.createBufferedImage(face.getFacePatch()), new File(""), "pissas" + i);
//						} else if (!face.getBounds().equals(aux.getBounds())) {
//							//saveFace(ImageUtilities.createBufferedImage(face.getFacePatch()), new File(""), "pissas"+ i);
//						} 
//						System.out.println(annotation.annotation + " has confidence of " + annotation.confidence);
//					}
//				}
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//
//		return (detected > 1) ? true : false;
//
//	}

	public static FaceRecognitionEngine createAndTrainRecognitionEngine(GroupedDataset dataset, String cascadeFolder) {
		FaceDetector<DetectedFace, FImage> haar = new HaarCascadeDetector(new File(cascadeFolder).listFiles()[2].getAbsolutePath());
		FKEFaceDetector faceDetector = new FKEFaceDetector(haar);
		FisherFaceRecogniser<KEDetectedFace, String> recognizer = FisherFaceRecogniser.create(18, new RotateScaleAligner(), 1, DoubleFVComparison.CORRELATION, 0.7f); 
		FaceRecognitionEngine engine = FaceRecognitionEngine.create(faceDetector,recognizer);
		engine.train(dataset);
		return engine;
	}


	private static void saveFace(DetectedFace face, File folder, String name) throws FileNotFoundException {
		File f = new File(folder.getAbsolutePath() + "/faces");
		if (!f.exists()){
			f.mkdirs();
		}
		try {
			File faceFile = new File(f.getAbsolutePath() + "/" + name + ".jpg");
			ImageUtilities.write(face.getFacePatch(), faceFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
