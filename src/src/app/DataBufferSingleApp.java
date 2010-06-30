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
import utils.DataSetBuilder;
import utils.Dataset;
import utils.DatasetBuffer;
import utils.DatasetBufferSingle;

public class DataBufferSingleApp {
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String pathToJDF = "/tmp";
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
			
			for (int i = 0; i < totalAnimal; i++) {
				Node animalNode = listOfAnimal.item(i);
				AnimalSetup animal = null;
				if (animalNode.getNodeType() == Node.ELEMENT_NODE) {

					animal = new AnimalSetup(animalNode);
					DatasetBufferSingle buffer = new DatasetBufferSingle(3000, "/tmp");

					if (animal != null) {
						animalList.add(animal);
						DataSetBuilder D = new DataSetBuilder(animal);
						D.saveFile(D.buildJDF(D.run(buffer, 10), "nda.jar"),pathToJDF+File.separatorChar+animal.getName()+".jdf");
												
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
