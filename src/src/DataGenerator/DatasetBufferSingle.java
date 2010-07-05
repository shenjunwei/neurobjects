package DataGenerator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

import errors.InvalidArgumentException;

public class DatasetBufferSingle {
	
	protected BlockingQueue<Dataset> data = null;
	//protected int maxSize=0;
	protected int maxAtts=0;
	protected int numAtts=0;
	protected String workDir="";
//	protected ZipHnd zipHnd = null;
	
	
	
		
	public DatasetBufferSingle (int max, String workDir ) throws InvalidArgumentException {
		
		if (max<=0) {			
			throw new InvalidArgumentException("Invalid max input values!!");  
		}
		
		File d = new File(workDir);
		if ((!d.isDirectory()) || (!d.canWrite())) {
			throw new InvalidArgumentException();  
		}
		this.workDir = workDir;
		this.maxAtts= max;
		this.data = new LinkedBlockingQueue<Dataset> ();
	//	this.zipHnd = new ZipHnd();
		
	}
	
	
	
	public synchronized int size () {				
		return this.data.size();
	}
	
	public synchronized boolean isEmpty () {
		if (this.data.size()==0) {
			return true;
		}
		return (false);
	}
	
	public synchronized boolean isFull () {
		if (this.numAtts>this.maxAtts) {
			return true;
		}		
		return (false);
	}
	
	public synchronized boolean add (Dataset d) {
		if (this.isFull()) {
			return false;
		}
		this.numAtts+=d.getNumAtts();
		this.data.add(d);
		return true;
	}
	
	
	public synchronized String toString () {
		String result = "Max attributes: "+this.maxAtts;
		
		if (this.data==null) {
			result += "object was not yet properly built." ;
			return (result);
		}
		result+="\t Total number of attributes:"+this.numAtts;
		result+="\t Number of datasets:"+this.data.size();
		return (result);
	}
	
	public synchronized void empty () {
		this.data.clear();
		this.numAtts = 0;
	}
	
	public synchronized void saveZip(String zipfilename) throws FileNotFoundException, IOException {
	
		FileOutputStream dest = new FileOutputStream(zipfilename);
        CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
        Dataset d = null;
        while (!this.data.isEmpty()) {
        	d = this.data.poll();
        	d.saveZip(out);
        }
        out.close();
        this.numAtts = 0;
        	
       

		
	}



	public String getWorkDir() {
		return workDir;
	}
	
	
}
