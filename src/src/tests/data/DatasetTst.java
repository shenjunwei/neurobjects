package tests.data;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
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

import utils.Properties;
import weka.core.Instances;

import DataGenerator.AnimalSetup;
import DataGenerator.DataSetBuilder;

import data.Dataset;

import errors.InvalidArgumentException;

public class DatasetTst {
	
	AnimalSetup animal = null;

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
	}

	/** Tests if the exception is correctly created */
	@Test (expected=InvalidArgumentException.class)  
	public void testExcpDatasetInstancesInstancesAnimalSetupStringString01 () throws InvalidArgumentException {
		Dataset D = new Dataset(null, null, null, null, null);
	}
	/** Tests if the exception is correctly created */
	@Test (expected=InvalidArgumentException.class)  
	public void testExcpDatasetInstancesInstancesAnimalSetupStringString02 () throws InvalidArgumentException {
		Dataset D = new Dataset(null, null, null, "", "");
	}
	
	@Test (expected=InvalidArgumentException.class)  
	public void testExcpDatasetInstancesInstancesAnimalSetupStringString03 () throws InvalidArgumentException, FileNotFoundException, IOException {
		String trainingFilename = "setup/datasets/10883428.trn.arff";
		String testFilename = "setup/datasets/10883428.tst.arff";
		
		Instances dataTrain = new Instances(
                new BufferedReader(
                                new FileReader(trainingFilename)));
		
		Instances dataTest = new Instances(
                new BufferedReader(
                                new FileReader(testFilename)));
		
		//AnimalSetup animal = null;
		
		Dataset d = new Dataset(dataTrain, dataTest, animal, "", "hp");
		//System.out.println (d);
	}
	
	
	@Test
	public void testDatasetInstancesInstancesAnimalSetupStringString() throws FileNotFoundException, IOException, InvalidArgumentException {
		String trainingFilename = "setup/datasets/10883428.trn.arff";
		String testFilename = "setup/datasets/10883428.tst.arff";
		
		Instances dataTrain = new Instances(
                new BufferedReader(
                                new FileReader(trainingFilename)));
		
		Instances dataTest = new Instances(
                new BufferedReader(
                                new FileReader(testFilename)));
		
		//AnimalSetup animal = null;
		
		Dataset d = new Dataset(dataTrain, dataTest, animal, "ball", "hp");
		System.out.println (d);
		
	
	}

	@Test
	public void testDatasetProperties() {
		
		String str="%window_width=10\n" +
				"bin_size=0.25\n" +
				"table_name=ioc_results_basic3\n" +
				"bmode=random\n" +
				"label=ball\n" +
				"area=hp\n" +
				"job=ge4.basic\n" +
				"animal=ge4";
		Properties P = new Properties (str);
		Dataset d = new Dataset(P);
		Assert.assertEquals(P.getValue("animal"), d.getAnimal());
		Assert.assertEquals(P.getValue("label"), d.getLabel());
		Assert.assertEquals(P.getValue("area"), d.getArea());
		
	}

	@Test
	public void testSaveZipString() throws FileNotFoundException, IOException, InvalidArgumentException {
		String trainingFilename = "setup/datasets/10883428.trn.arff";
		String testFilename = "setup/datasets/10883428.tst.arff";
		
		Instances dataTrain = new Instances(
                new BufferedReader(
                                new FileReader(trainingFilename)));
		
		Instances dataTest = new Instances(
                new BufferedReader(
                                new FileReader(testFilename)));
		
		//AnimalSetup animal = null;
		
		Dataset d = new Dataset(dataTrain, dataTest, animal, "ball", "hp");
		d.saveZip("/tmp/teste.zip");
	}


}
