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

import data.Dataset;


/**
 * \brief Models a single buffer of Dataset
 * 
 * 
 * This class models an object that is a buffer Dataset. It should store the
 * Dataset's until they are saved in a zip file, or even an operation to empty the
 * buffer. The Dataset's are stored in a order of arrival queue.
 * 
 * @author Nivaldo Vasconcelos
 * @date 07July2010
 * 
 */
public class DatasetBufferSingle {
	
	protected BlockingQueue<Dataset> data = null;
	protected int maxAtts=0;
	protected int numAtts=0;
	protected String workDir="";
	
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/**
	 * \brief Creates a dataset buffer with a single queue
	 * 
	 * Given maximum size (maximum number of dataset in the queue) for the
	 * queue, \c max, and a path to a working directory, \c workDir, creates a
	 * DataSetBufferSingle. In that working directory will be created that data
	 * files.
	 * 
	 * @param max
	 *            maximum number of datasets in the queue;
	 * @param workDir
	 *            working directory;
	 * @throws IllegalArgumentException
	 */
	public DatasetBufferSingle (int max, String workDir ) throws IllegalArgumentException {
		
		if (max<=0) {			
			throw new IllegalArgumentException("Invalid max input values!!");  
		}
		
		File d = new File(workDir);
		if ((!d.isDirectory()) || (!d.canWrite())) {
			throw new IllegalArgumentException();  
		}
		this.workDir = workDir;
		this.maxAtts= max;
		this.data = new LinkedBlockingQueue<Dataset> ();
	
		
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/** \brief Returns the current number of datasets in the buffer
	 *  
	 * @return the current number of datasets in the buffer 
	 */
	public synchronized int size () {				
		return this.data.size();
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/** \brief Tells if the buffer is empty.
	 * 
	 * @return \c true if the buffer is empty, or \c false otherwise.
	 */
	public synchronized boolean isEmpty () {
		if (this.data.size()==0) {
			return true;
		}
		return (false);
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/** \brief Tells if the buffer is full.
	 * @return \c true if the buffer is full, or \c false otherwise.
	 */
	public synchronized boolean isFull () {
		if (this.numAtts>this.maxAtts) {
			return true;
		}		
		return (false);
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/** \brief Adds a dataset to buffer  
	 * @param d dataset to be inserted
	 * @return  \c true if the dataset was inserted into buffer successfully , or \c false otherwise.
	 */
	public synchronized boolean add (Dataset d) {
		if (this.isFull()) {
			return false;
		}
		this.numAtts+=d.getNumAtts();
		this.data.add(d);
		return true;
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/**  \brief Returns a string representation of the buffer.
	 *  
	 * @return a String with buffer informations.
	 */
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
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/**
	 * \brief Clear the buffer
	 * 
	 * After this there is no content in the buffer and will be clear also the
	 * number of attributes.
	 * 
	 */
	public synchronized void empty () {
		this.data.clear();
		this.numAtts = 0;
	}

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/** \brief Saves the buffer content into a zip file
	 * 
	 * To implement the dataset saving operation calls the Dataset.saveZip
	 * methods.
	 * 
	 * @param zipfilename
	 *            name of file in which will created a zip representation of the
	 *            set of datasets existing in the buffer;
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public synchronized void saveZip(String zipfilename)
			throws FileNotFoundException, IOException {

		FileOutputStream dest = new FileOutputStream(zipfilename);
		CheckedOutputStream checksum = new CheckedOutputStream(dest,
				new Adler32());
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
				checksum));
		Dataset d = null;
		while (!this.data.isEmpty()) {
			d = this.data.poll();
			d.saveZip(out);
		}
		out.close();
		this.numAtts = 0;

	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */



	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	/** \brief Returns the working directory used by the buffer 
	 * @return the working directory used by the buffer 
	 */
	public String getWorkDir() {
		return workDir;
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	
}
