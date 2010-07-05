package app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import DataGenerator.AnimalSetup;


public class DataHolderApp2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = "/home/nivaldo/tmp/nda/animal_file_setup_ge4.xml";
		ArrayList<AnimalSetup> animalList = new ArrayList<AnimalSetup>(); 
		String filename = "/home/nivaldo/tmp/nda/data/teste.obj";
		
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
						DataHolder holder = new DataHolder(animal);
						
						FileOutputStream fos = null;
						ObjectOutputStream out = null;
						try
						{
						fos = new FileOutputStream(filename);
						out = new ObjectOutputStream(fos);
						out.writeObject(holder);
						out.close();
						}
						catch(IOException ex)
						{
						ex.printStackTrace();
						}
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
