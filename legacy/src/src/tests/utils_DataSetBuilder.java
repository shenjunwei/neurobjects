package tests;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import DataGenerator.AnimalSetup;
import DataGenerator.DataSetBuilder;
import DataGenerator.Setup;

import weka.core.Instances;

public class utils_DataSetBuilder {

	/**
	 * \brief This class tests the src.utils.DataSetBuilder class
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		

		String configFile = "/tmp/animal_file_setup_ge5.xml";
		ArrayList<AnimalSetup> animalList = new ArrayList<AnimalSetup>();
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse (new File(configFile));
		// normalize text representation
		doc.getDocumentElement().normalize();
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
				
					//OK unknow filter and unknow label
					//Instances dataset_tmp[] = D.getInstances("../../", "../../../");
					//System.out.println (dataset_tmp[0]);
					//System.out.println (dataset_tmp[1]);
					//System.out.println("\n\n");

					
					
					
					Instances dataset_ball[] = D.getInstances("v1", "ball");
					System.out.println (dataset_ball[0]);
					System.out.println (dataset_ball[1]);
					System.out.println("\n\n");

				/*	Instances dataset_brush[] = D.getInstances("v1", "brush");
					System.out.println (dataset_brush[0]);
					System.out.println (dataset_brush[1]);
					System.out.println("\n\n");

					
					Instances dataset_urchin[] = D.getInstances("v1", "urchin");
					System.out.println (dataset_urchin[0]);
					System.out.println (dataset_urchin[1]);
					System.out.println("\n\n");
					
					Instances dataset_food[] = D.getInstances("v1", "food");
					System.out.println (dataset_food[0]);
					System.out.println (dataset_food[1]);
					System.out.println("\n\n");
*/
					return;
				}
			}
			
		}		
		
		
		
	}

}
