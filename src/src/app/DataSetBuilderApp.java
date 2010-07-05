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

import weka.core.Instances;

public class DataSetBuilderApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String configFile = "/home/nivaldo/tmp/nda/animal_file_setup_ge5.xml";
		ArrayList<AnimalSetup> animalList = new ArrayList<AnimalSetup>(); 
		
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
					if (animal != null) {
						animalList.add(animal);
						DataSetBuilder D = new DataSetBuilder(animal);
						System.out.println(animal);
						Dataset data = D.get("hp", "ball");
						System.out.println (data);
						
						System.out.println("\n\n");
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
