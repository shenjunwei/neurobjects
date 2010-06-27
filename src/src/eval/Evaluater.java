package eval;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import utils.Context;
import utils.Dataset;
import utils.DatasetBuffer;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import errors.InvalidArgumentException;

public class Evaluater {
	
	static final int BUFFER = 1024*1024;
	weka.classifiers.Classifier cModel = null;
	String 						models[] = {"NBayes","MLP","J48","RBF","SVM"};
	ArrayList<String> 			supportedModels=null;
	ArrayList<Dataset>			data=null;
	String 						dataFiles[]=null;			
	
	
	public Evaluater (String dataFiles[]) {
		
		this.buildModelList();
		this.dataFiles = dataFiles;
		
	}
	
	public Evaluater (String zipFilename) throws IOException {
		this.buildModelList();
		this.data = new ArrayList<Dataset> ();
		
		BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(zipFilename);
        CheckedInputStream checksum = new 
          CheckedInputStream(fis, new Adler32());
        ZipInputStream zis = new 
          ZipInputStream(new 
            BufferedInputStream(checksum));
        ZipEntry entry;
        while((entry = zis.getNextEntry()) != null) {
           System.out.println("Extracting: " +entry);
           int count;
           byte data[] = new byte[BUFFER];
           String str = null;
           String result = ""; 
           while ((count = zis.read(data, 0, 
             BUFFER)) != -1) {
        	   str = new String (data,"UTF-8");
        	   result+=str;
        	   	
           }
           StringReader strReader = new StringReader (result);
           Instances tmpData = new Instances (strReader);
        }
        zis.close();
		
		
	}
	
	private void buildModelList () {
		this.supportedModels = new ArrayList<String>();
		int numModels = this.models.length;
		int i=0;
		
		for (i=0; i<numModels; i++) {
			this.supportedModels.add (this.models[i]);
		}
		
	}
	
	public synchronized void unzip (String dir, String zipfilename) throws IOException {
		
		BufferedOutputStream dest = null;
        FileInputStream fis = new 
	   FileInputStream(zipfilename);
        CheckedInputStream checksum = new 
          CheckedInputStream(fis, new Adler32());
        ZipInputStream zis = new 
          ZipInputStream(new 
            BufferedInputStream(checksum));
        ZipEntry entry;
        while((entry = zis.getNextEntry()) != null) {
           System.out.println("Extracting: " +entry);
           int count;
           byte data[] = new byte[BUFFER];
           // write the files to the disk
           FileOutputStream fos = new 
             FileOutputStream(entry.getName());
           dest = new BufferedOutputStream(fos, 
             BUFFER);
           while ((count = zis.read(data, 0, 
             BUFFER)) != -1) {
        	   dest.write(data, 0, count);
           }
           dest.flush();
           dest.close();
        }
        zis.close();
        System.out.println("Checksum:  "+checksum.getChecksum().getValue());
		
	}
	
	
	public synchronized void  runAll () throws FileNotFoundException, IOException {
		
		String trainFilename = "";
		String testFilename = "";
		Evaluation e = null;
		for (int i = 0; i < this.dataFiles.length; i++) {
			trainFilename = this.dataFiles[i];
			testFilename = trainFilename.replaceFirst(".trn.", ".tst.");
			for (int j = 0; j < this.models.length; j++) {
				this.setModel(this.models[j]);
				e = this.eval(trainFilename, testFilename);
			}
		}
	}
	
	private synchronized Evaluation eval(String trainFilename,
			String testFilename) {
		Instances dataTrain = null;
		try {
			dataTrain = new Instances(new BufferedReader(new FileReader(
					trainFilename)));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Test set
		Instances dataTest = null;
		try {
			dataTest = new Instances(new BufferedReader(new FileReader(
					testFilename)));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
		dataTest.setClassIndex(dataTest.numAttributes() - 1);
		try {
			cModel.buildClassifier(dataTrain);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Evaluation eval = null;
		try {
			eval = new Evaluation(dataTrain);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			eval.evaluateModel(cModel, dataTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (eval);

	}

	
private synchronized void  setModel (String model) {
		
		this.cModel = null;
		if (model.equals("NBayes")) {
			this.cModel = new weka.classifiers.bayes.NaiveBayes();	
		}
		if (model.equals("MLP")) {
			this.cModel = new weka.classifiers.functions.MultilayerPerceptron();
		}
		if (model.equals("J48")) {
			this.cModel = new weka.classifiers.trees.J48();
		}
		if (model.equals("RBF")) {
			this.cModel = new weka.classifiers.functions.RBFNetwork();
		}
		if (model.equals("SVM")) {
			this.cModel = new weka.classifiers.functions.SMO();
			try {
				this.cModel.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (this.cModel==null) {
			//TODO creates a exception !!
			new InvalidArgumentException("Unknown model: " + model);
			return;
		}
		
	}

}
