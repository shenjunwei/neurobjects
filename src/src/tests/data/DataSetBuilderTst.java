package tests.data;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.BuildMode;

import DataGenerator.AnimalSetup;
import DataGenerator.DatasetBufferSingle;


public class DataSetBuilderTst {
	
	AnimalSetup animal=null;

	@Before
	public void setUp() throws Exception {
		String configFile = "setup/ge4_setup.xml";

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new File(configFile));
			doc.getDocumentElement ().normalize ();	// normalize text representation
			NodeList listOfAnimal = doc.getElementsByTagName("animal");
			ArrayList<AnimalSetup> animalList = new ArrayList<AnimalSetup>(); 


			for (int i=0; i<listOfAnimal.getLength(); i++) {
				Node animalNode = listOfAnimal.item(i);			
				if (animalNode.getNodeType() == Node.ELEMENT_NODE) {
					this.animal = new AnimalSetup(animalNode);
					if (this.animal != null) {
						animalList.add(this.animal);
						
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	@After
	public void tearDown() throws Exception {
		animal=null;
	}

	@Test
	public void testDataSetBuilderAnimalSetup() throws IllegalArgumentException {
		
		DataGenerator.DataSetBuilder d = new DataGenerator.DataSetBuilder (animal);
		Assert.assertEquals((int) (Math.floor(this.animal.getTotalSamples()*this.animal.getAlfa())), d.getNumPositiveSamplesToTrain());
		Assert.assertEquals((int) (animal.getTotalSamples()-d.getNumPositiveSamplesToTrain()), d.getNumPositiveSamplesToTest());
		
		
	}

	@Test
	public void testDataSetBuilderAnimalSetupBuildMode() throws IllegalArgumentException {
		BuildMode bmode = BuildMode.RANDOM;
		DataGenerator.DataSetBuilder d = new DataGenerator.DataSetBuilder (animal,bmode);
		System.out.println (d);
	}
	

	@Test
	public void testRun() throws Exception {
		BuildMode bmode = BuildMode.RANDOM;
		DatasetBufferSingle buffer = new DatasetBufferSingle(2000, "/tmp/");
		DataGenerator.DataSetBuilder D = new DataGenerator.DataSetBuilder (animal,bmode);
		ArrayList<String> zipfiles = D.run(buffer, "teste", "jobNameTest", 1,bmode);
		System.out.println (zipfiles);
		D.showPatterns();
	}

	@Test
	public void testGet() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveFile() {
		fail("Not yet implemented");
	}

}
