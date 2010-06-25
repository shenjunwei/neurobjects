package app;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import utils.AnimalSetup;
import utils.DataGenerator;
import utils.DataSetBuilder;
import utils.Dataset;
import utils.DatasetBuffer;
import utils.ModelEvaluater;

public class DataGeneratorApp {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
// TODO Auto-generated method stub
		
		String configFile = "setup/ge4_setup.xml";
		ArrayList<AnimalSetup> animalList = new ArrayList<AnimalSetup>(); 
		String modelName[] = {"NBayes","MLP","J48","SVM","RBF"};
		int	bufferSize = 10;
		int numOfInstances = 10;
		int numOfEvaluaters = (Runtime.getRuntime().availableProcessors()-1);
		if (numOfEvaluaters<1) {
			numOfEvaluaters = 1;
		}
		
		ArrayList<String> models = new ArrayList<String> ();
		for (int i=0; i<modelName.length; i++) {
			models.add(modelName[i]);
		}
		
		
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new File(configFile));

			// normalize text representation
			doc.getDocumentElement ().normalize ();

			NodeList listOfAnimal = doc.getElementsByTagName("animal");
			int totalAnimal = listOfAnimal.getLength();
		//	System.out.println ("Number of animal description in XML file: " +totalAnimal);
		
			ModelEvaluater evaluater[] = new ModelEvaluater[numOfEvaluaters] ;
			for (int i = 0; i < totalAnimal; i++) {
				Node animalNode = listOfAnimal.item(i);
				AnimalSetup animal = null;
				if (animalNode.getNodeType() == Node.ELEMENT_NODE) {

					animal = new AnimalSetup(animalNode);
					DatasetBuffer buffer = new DatasetBuffer(models, bufferSize);

					if (animal != null) {
						animalList.add(animal);
						DataGenerator DG = new DataGenerator(animal, buffer, numOfInstances);
						DG.start();
						Thread.sleep(100);
						for (int k=0; k<numOfEvaluaters; k++) {
							evaluater[k] = new ModelEvaluater (buffer);
							evaluater[k].start();
						}
						
						System.out.println ("Pausing 4s ...");
						while (true) {
							Thread.sleep(2000);
							// System.out.println (buffer);
							if ((buffer.isEmpty()) && (DG.isDone())) {
								System.out.println(buffer);
								for (int j = 0; j < evaluater.length; j++) {
									if (evaluater[j] != null) {
										evaluater[j].setDone(true);
									}
								}
								System.out.println("All done");
								return;
							}

						}
						//return;
					}
				}
				
			}
		}catch (SAXParseException err) {
			System.out.println ("** Parsing error" + ", line " 
					+ err.getLineNumber () + ", uri " + err.getSystemId ());
			System.out.println(" " + err.getMessage ());


		}catch (SAXException e) {
			Exception x = e.getException ();
			((x == null) ? e : x).printStackTrace ();


		}catch (Throwable t) {
			t.printStackTrace ();

		}
		
	}


}
