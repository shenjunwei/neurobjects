package tests;

import static org.junit.Assert.assertThat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestCase;

import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.matchers.JUnitMatchers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import weka.core.Instances;
import DataGenerator.AnimalSetup;
import DataGenerator.DataSetBuilder;
import DataGenerator.Dataset;

public class DataGenerator_DataSet extends TestCase implements Test {
	
	private ArrayList<Dataset> dataSetList = new ArrayList<Dataset>();
	private final int minimumInstances = 5;
	private final int minimumAttributes =1;
	private final int minimumWindowWidth=1;
	
	public DataGenerator_DataSet() {
		super();
	}
	
	
	/**
	 * Aqui todo o contexto necessário para instanciar o objeto deverá ser criado.
	 */
	@Before
	protected void setUp() {
		String configFile = "/home/ambar/tmp/animal_file_setup_ge5.xml";

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
				AnimalSetup animal = null;
				if (animalNode.getNodeType() == Node.ELEMENT_NODE) {
					animal = new AnimalSetup(animalNode);
					if (animal != null) {
						animalList.add(animal);
						DataSetBuilder D = new DataSetBuilder(animal);
						this.dataSetList.add(D.get("hp", "ball"));
						this.dataSetList.add(D.get("s1", "ball"));
						this.dataSetList.add(D.get("v1", "ball"));

						this.dataSetList.add(D.get("hp", "brush"));
						this.dataSetList.add(D.get("s1", "brush"));
						this.dataSetList.add(D.get("v1", "brush"));

						this.dataSetList.add(D.get("hp", "urchin"));
						this.dataSetList.add(D.get("s1", "urchin"));
						this.dataSetList.add(D.get("v1", "urchin"));
						
						this.dataSetList.add(D.get("hp", "food"));
						this.dataSetList.add(D.get("s1", "food"));
						this.dataSetList.add(D.get("v1", "food"));
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
	// --- //
	
	
	/**
	 * Aqui devemos destruir o contexto usado para instanciar o objeto.
	 */
	@After
	protected void tearDown() {
		this.dataSetList = null;
	}
	// --- //
	
	
	public void  testSave() throws UnsupportedEncodingException, FileNotFoundException {
		for (Dataset dataSet : this.dataSetList) {
			dataSet.save("/tmp");			
		}
		
	}
	// --- //
	
	public void testGetTrainFilename() {
		for (Dataset dataSet : this.dataSetList) {
			assertNotNull("O objeto retornou NULL", dataSet.getTrainFilename());
			assertTrue(dataSet.isTrainingFilename(dataSet.getTrainFilename()));
		}
	}
	// --- //
	
	
	public void testGetTstFileName() {
		for (Dataset dataSet : this.dataSetList) {
			assertNotNull("O objeto retornou NULL", dataSet.getTstFilename());
			assertTrue(dataSet.isTestingFilename(dataSet.getTstFilename()));
		}
	}
	// --- //
	
	public void testSaveZip() throws IOException {
		for (Dataset dataSet : this.dataSetList) {
			String zipFileName = "/tmp/"+dataSet.getAnimal()+"_"+dataSet.getArea()+"_"+dataSet.getLabel()+"_"+dataSet.getBinSize()+"_"+dataSet.getWindowWidth()+".zip";
			dataSet.saveZip(zipFileName);
			
			 ZipFile zip = new ZipFile(zipFileName);
		        
		     assertNotNull("O zip gerado está corrompido", zip);
		     //The zipe file should have 2 files: train and test.
		     assertEquals(2, zip.size());
		}	
	}
	// --- //
	
	public void testSaveZipOutputStream() throws IOException {
		for (Dataset dataSet : this.dataSetList) {
			//String zipFileName = "/tmp/OutputStream_"+dataSet.getAnimal()+"_"+dataSet.getArea()+"_"+dataSet.getLabel()+"_"+dataSet.getBinSize()+"_"+dataSet.getWindowWidth()+".zip";
			
			String zipFileName = "/tmp/OutputStream_ANIBAL.zip";
					
			FileOutputStream dest = new FileOutputStream(zipFileName);
			CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					checksum));

	        dataSet.saveZip(out);
	        out.close();
	        
	        ZipFile zip = new ZipFile(zipFileName);
	        
	        assertNotNull("O zip gerado está corrompido", zip);
	        assertEquals(2, zip.size());  //The zipe file should have 2 files: train and test.

		}
	}
	// --- //
	
	public void testIsValid() {
		for (Dataset dataSet : this.dataSetList) {
			assertTrue(dataSet.isValid());			
		}
	}
	// --- //
	
	
	@org.junit.Test
	public void testGetTrainData() {
		for (Dataset dataSet : this.dataSetList) {
			//Verify if result data is of type weka.core.Instances
			assertThat(dataSet.getTrainData(), IsInstanceOf.instanceOf(Instances.class));
			//Verify if result data has the minimum instance size.
			assertTrue((dataSet.getTrainData().numInstances()>=this.minimumInstances));
			//Verify if result data has the minimum attribute size.
			assertTrue(dataSet.getTrainData().numAttributes()>=this.minimumAttributes);
		}
	}
	// --- //
	
	@org.junit.Test
	public void testGetTestData() {
		for (Dataset dataSet : this.dataSetList) {
			//Verify if result data is of type weka.core.Instances
			assertThat(dataSet.getTestData(), IsInstanceOf.instanceOf(Instances.class));
			//Verify if result data has the minimum instance size.
			assertTrue((dataSet.getTestData().numInstances()>=this.minimumInstances));
			//Verify if result data has the minimum attribute size.
			assertTrue(dataSet.getTestData().numAttributes()>=this.minimumAttributes);
		}
	}
	// --- //
	
	@org.junit.Test
	public void testGetAnimal() {
		for (Dataset dataSet : this.dataSetList) {
			assertThat(dataSet.getAnimal(), JUnitMatchers.containsString("ge"));
		}
	}
	// --- //
	
	@org.junit.Test
	public void testGetLabel() {
		for (Dataset dataSet : this.dataSetList) {
			assertThat(dataSet.getLabel(), 
					JUnitMatchers.either(JUnitMatchers.containsString("ball"))
					.or(JUnitMatchers.containsString("brush"))
					.or(JUnitMatchers.containsString("urchin"))
					.or(JUnitMatchers.containsString("food")));
		}		
	}
	// --- //
	
	@org.junit.Test
	public void testGetArea() {
		for (Dataset dataSet : this.dataSetList) {
			assertThat(dataSet.getArea(), 
					JUnitMatchers.either(JUnitMatchers.containsString("hp"))
					.or(JUnitMatchers.containsString("s1"))
					.or(JUnitMatchers.containsString("v1")));
		}
	}
	// --- //
	
	@org.junit.Test
	public void testGetBinSize() {
		for (Dataset dataSet : this.dataSetList) {
			//Verify if the bin size is greater than zero.
			assertTrue(dataSet.getBinSize()>Double.NEGATIVE_INFINITY);
		}
	}
	// --- //
	
	@org.junit.Test
	public void testGetWindowWidth() {
		for (Dataset dataSet : this.dataSetList) {
			//Verify if the window width is greater than minimum window width.
			assertTrue(dataSet.getWindowWidth()>=minimumWindowWidth);
		}
	}
	// --- //
	
	@org.junit.Test
	public void testGetNumAtts() {
		for (Dataset dataSet : this.dataSetList) {
			assertTrue(dataSet.getNumAtts()>=minimumAttributes);
		}
	}
	// --- //
	
	
}
