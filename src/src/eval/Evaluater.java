package eval;

/*
 * 
 * 
 * Evaluation eval = new Evaluation(dataTrain);
                eval.evaluateModel(cModel, dataTest);

                C.setAUROC(eval.weightedAreaUnderROC());
                C.setFMeasure(eval.fMeasure(0));
                C.setKappa(eval.kappa());
                C.setPctCorrect(eval.pctCorrect());
                C.setEndTime(System.currentTimeMillis());

                C.showSQL("ioc_results_basic2");
*/
 

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import DataGenerator.Dataset;
import DataGenerator.DatasetBuffer;
import DataGenerator.Properties;

import utils.Context;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import errors.InvalidArgumentException;

public class Evaluater {
	static final int 					KB = 1024;
	static final int 					MB = 1024*KB;
	static final int 					BUFFER_SIZE = 1*MB;
	weka.classifiers.Classifier 		cModel = null;
	String 								models[] = {"NBayes","MLP","J48","RBF","SVM"};
	ArrayList<String> 					supportedModels=null;
	protected Hashtable<String, Dataset> data = null;
	
	
	
	
	public Evaluater (String dataFiles[]) {
		
		this.buildModelList();
		
		
		
	}
	
	public Evaluater (String zipFilename) throws IOException {
		this.buildModelList();
		this.data = new Hashtable<String, Dataset> ();
		
		
        FileInputStream fis = new FileInputStream(zipFilename);
        CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
        this.getAllData(zis);
        zis.close();
		
		
	}
	
	private String getDatasetName(ZipEntry entry) {
		int pos1, pos2;
		String name = "";
		String datasetName = "";
		name = entry.getName();
		pos1 = name.indexOf('/');
		pos2 = name.indexOf('/', pos1 + 1);
		datasetName = name.substring(pos1 + 1, pos2 - 1);
		return datasetName;

	}
	
	
	
	private void getAllData(ZipInputStream zis) throws UnsupportedEncodingException, IOException {
		
		ZipEntry entry;
		String datasetName = "";
		String filename = "";
		int count;
		String str = null;
		String result = "";
       
        Dataset datasetTmp = null;
        byte data[] = new byte[BUFFER_SIZE];
		while ((entry = zis.getNextEntry()) != null) {
			datasetName = this.getDatasetName(entry);
			filename = entry.getName();
			if (!entry.isDirectory()) {
				System.out.println("Unziping: " + datasetName);
				result="";
				while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
					str = new String(data, 0, count, "UTF-8");
					result += str;
				}
				Properties prop = new Properties(this.getSetupInfo(result));
				BufferedReader reader = new BufferedReader(new StringReader(result));
				Instances tmpData = new Instances(reader);
				
				if (this.data.containsKey(datasetName)) {
					datasetTmp = this.data.get(datasetName);
				} else {
					datasetTmp = new Dataset(prop);
				}
				
				if (datasetTmp.isTrainingFilename(filename)) {
					datasetTmp.setTrainData(tmpData);
				}
				else if (datasetTmp.isTestingFilename(filename)) {
					datasetTmp.setTestData(tmpData);
				}
				this.data.put(datasetName, datasetTmp); 

			}

		}

	}
	
	private String getSetupInfo (String data) {
		
		int pos1 = data.indexOf("<setup>");
        int pos2 = data.indexOf("</setup>",pos1+1);
		String setup = data.substring(pos1+"<setup>".length(), pos2-1);
		setup= setup.replace("\n%", "\n");
		return (setup);
		
	}
	
	private void buildModelList () {
		this.supportedModels = new ArrayList<String>();
		int numModels = this.models.length;
		int i=0;
		
		for (i=0; i<numModels; i++) {
			this.supportedModels.add (this.models[i]);
		}
		
	}
	
	
	
	
	public synchronized void  runAll () throws FileNotFoundException, IOException {
		
		
		Evaluation e = null;
		String SQLQuery="";
		Enumeration<String> d = this.data.keys();
		Dataset data=null;
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long begin_time,end_time,duration;
        
		while (d.hasMoreElements()) {
			data = this.data.get(d.nextElement()); 
			for (int j = 0; j < this.models.length; j++) {
				this.setModel(this.models[j]);
				cal = Calendar.getInstance();
				data.getProperties().setProperty("time", sdf.format(cal.getTime()));
				begin_time =  System.currentTimeMillis();
				e = this.eval(data);
				end_time =  System.currentTimeMillis();
				data.getProperties().setProperty("model", this.models[j]);
				duration = end_time-begin_time;
				data.getProperties().setProperty("duration", duration+"");
				data.getProperties().setProperty("status", "OK");
				this.recordEval(data, e);
				SQLQuery=data.getProperties().toSQLString(); 
				if (SQLQuery.isEmpty()) {
					System.out.println ("Problems SQL Query generation "+data.getProperties());
				}
				else {
					System.out.println (SQLQuery);
				}
				
				
			}
			
		}
		
	}
	
	
	private void recordEval (Dataset data,Evaluation e) {
		data.getProperties().setProperty("auroc", e.weightedAreaUnderROC()+"");
		data.getProperties().setProperty("fmeasure", e.fMeasure(0)+"");
		data.getProperties().setProperty("kappa", e.kappa()+"");
		data.getProperties().setProperty("pctcorrect", e.pctCorrect()+"");
		data.getProperties().setProperty("pctcorrect", e.pctCorrect()+"");
		
	}
	
	private synchronized Evaluation eval(Dataset data) {
		Instances dataTrain = data.getTrainData();
		Instances dataTest = data.getTestData();
		
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
