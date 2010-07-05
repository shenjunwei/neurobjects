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

import DataGenerator.AnimalSetup;
import DataGenerator.DataSetBuilder;
import DataGenerator.Dataset;
import DataGenerator.DatasetBuffer;

import utils.ModelEvaluater;

public class ModelEvaluaterApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
		String configFile = "/home/nivaldo/tmp/nda/animal_file_setup_ge5.xml";
		ArrayList<AnimalSetup> animalList = new ArrayList<AnimalSetup>(); 
		String modelName[] = {"NBayes","MLP","J48","SVM","RBF"};
		
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
			System.out.println ("Number of animal description in XML file: " +totalAnimal);
		
			
			for (int i = 0; i < totalAnimal; i++) {
				Node animalNode = listOfAnimal.item(i);
				AnimalSetup animal = null;
				if (animalNode.getNodeType() == Node.ELEMENT_NODE) {

					animal = new AnimalSetup(animalNode);
					DatasetBuffer buffer = new DatasetBuffer(models, 10);
					
					String area = "hp";
					String label = "ball";
					if (animal != null) {
						animalList.add(animal);
						DataSetBuilder D = new DataSetBuilder(animal);
						System.out.println(animal);
						Dataset data = D.get(area, label);
						buffer.add(data);
			/*			ModelEvaluater NBayes= new ModelEvaluater(animal,buffer,"NBayes",area,label).start();
					    new ModelEvaluater(animal,buffer,"J48",area,label).start();
					    new ModelEvaluater(animal,buffer,"MLP",area,label).start();
					    new ModelEvaluater(animal,buffer,"SVM",area,label).start();
					    new ModelEvaluater(animal,buffer,"RBF",area,label).start();
					    new ModelEvaluater(animal,buffer,"J48",area,label).start(); */
						//System.out.println (data);						
						return;
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
