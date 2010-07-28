package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import data.Dataset;

import errors.InvalidArgumentException;

import utils.BuildMode;

import DataGenerator.AnimalSetup;
import DataGenerator.DataSetBuilder;
import DataGenerator.DatasetBuffer;
import DataGenerator.DatasetBufferSingle;


public class DataBufferSingleApp {
	
/* 
 pathToJDF=/tmp
 tableName=ioc_results_basic3
 pathToXMLCfg=/home/nivaldo/projects/s1paper/grid/jobs/basic_window_05
 pathToApp=/tmp/nda_basic201007011214.jar
 jobName=basicNDA
 
  
 * */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String cfgFilename = args[0].trim(); 
		String str="";
		String info = "";
		
		
		/** \todo Implements more tests about the cfgFilename: existence, permission, etc */
		try {
			BufferedReader in = new BufferedReader(new FileReader(cfgFilename));

			while ((str = in.readLine()) != null) {
				info += str+"\n";
			}
			in.close();
		} catch (IOException e) {
		}
		utils.Properties prop = new utils.Properties(info);
		String pathToJDF = prop.getValue("pathToJDF");
		String tableName = prop.getValue("tableName");
		String pathToXMLCfg = prop.getValue("pathToXMLCfg");
		String pathToApp = prop.getValue("pathToApp");
		String dirLib = prop.getValue("dirLib");
		String jobName = prop.getValue("jobName");
		String NTPHost = prop.getValue("NTPHost");
		int numOfSamples = Integer.parseInt(prop.getValue("numOfSamples"));
		String bMode = prop.getValue("bMode");
		bMode = bMode.toLowerCase();
		BuildMode bmode=BuildMode.RANDOM;
		
		// Processing Build Mode
		if (bMode.equals("random")) {
			bmode = BuildMode.RANDOM;
		} else {
			if (bMode.equals("equals")) {
				bmode = BuildMode.EQUALS;
			} else {
				new InvalidArgumentException("Invalid value to bMode: " + bMode);
			}
		}
		
		ArrayList<AnimalSetup> animalList = new ArrayList<AnimalSetup>(); 
		
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new File(pathToXMLCfg));

			// normalize text representation
			doc.getDocumentElement ().normalize ();

			NodeList listOfAnimal = doc.getElementsByTagName("animal");
			int totalAnimal = listOfAnimal.getLength();
			
			for (int i = 0; i < totalAnimal; i++) {
				Node animalNode = listOfAnimal.item(i);
				AnimalSetup animal = null;
				if (animalNode.getNodeType() == Node.ELEMENT_NODE) {

					animal = new AnimalSetup(animalNode);
					DatasetBufferSingle buffer = new DatasetBufferSingle(2000, pathToJDF);

					if (animal != null) {
						animalList.add(animal);
						DataSetBuilder D = new DataSetBuilder(animal,bmode);

						ArrayList<String> zipfiles = D.run(buffer, tableName, jobName, numOfSamples,bmode);
						System.out.println (zipfiles);

						D.saveFile(D.buildJDF(zipfiles, pathToApp, dirLib, NTPHost),pathToJDF+File.separatorChar+animal.getName()+".jdf");

												
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
