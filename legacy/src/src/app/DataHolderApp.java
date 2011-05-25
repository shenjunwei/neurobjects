package app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

import weka.core.Instances;

public class DataHolderApp {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		
		//Training set
        String trainFilename="/home/nivaldo/tmp/nda/data/ge9.ball.s1.250.10.01.trn.arff";
        String testFilename="/home/nivaldo/tmp/nda/data/ge9.ball.s1.250.10.01.tst.arff";
        String filename = "/home/nivaldo/tmp/nda/data/teste.obj";
        
		Instances dataTrain = new Instances(
                        new BufferedReader(
                                        new FileReader(trainFilename)));
        // Test set
        Instances dataTest = new Instances(
                        new BufferedReader(
                                        new FileReader(testFilename)));


        dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
        dataTest.setClassIndex(dataTest.numAttributes() - 1);
		
		DataHolder holder = new DataHolder(dataTrain, dataTest);
		
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
