package DataGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import errors.EmptySourceException;
import errors.InvalidArgumentException;

/** \brief Models a set of buffers of dataset */
public class DatasetBuffer {
	
	protected Hashtable<String, BlockingQueue<Dataset>> data = null;
	protected ArrayList<String> models = null;
	protected int maxSize=0;
	
	//int dimension=-1;
	//String label="";
	
	public DatasetBuffer (ArrayList<String> models, int max ) throws InvalidArgumentException {
		
		if (models==null) {
			System.out.println ("Null pointer as model list input ");
			throw new InvalidArgumentException(); 
   	 
		}
		if (models.size()==0) {
			System.out.println ("Empty models input !!");
			throw new InvalidArgumentException();  
		}
		
		if (max<=0) {
			System.out.println ("Invalid max input values!!");
			throw new InvalidArgumentException();  
			
		}
		this.maxSize = max;
		this.models = new ArrayList<String> ();
		this.models.addAll(models);
		this.data = new Hashtable<String, BlockingQueue<Dataset>> ();
		this.createHashTable();
		
	}
	
	private synchronized void createHashTable () {
		Enumeration<String> e = Collections.enumeration(this.models);
		BlockingQueue<Dataset> list = null;
	    while(e.hasMoreElements()) {
	    	list = new LinkedBlockingQueue<Dataset>  ();
	    	this.data.put(e.nextElement(),list);
	    }
	}
	
	public synchronized int size () {
		
		int total = 0;
		Enumeration<String> e = this.data.keys();
		if (e==null) {
	    	return 0;
	    	// Would be better log this kind of event: e equal to null.
	    }
		while(e.hasMoreElements()) {
	    	total+=this.data.get(e.nextElement()).size();
	    }
		return total;
	}
	
	public synchronized boolean isEmpty () {
	//	System.out.println ("isEmpty ?");
		if (this.size()==0) {
			
			return true;
		}
		return (false);
	}
	
	public synchronized boolean isFull () {
		
		boolean result = false;
		
		Enumeration<String> e = this.data.keys();
		while (e.hasMoreElements()) {
			if (this.data.get(e.nextElement()).size() == this.maxSize) {
				result = true;
			}
		}
		return (result);
	}
	
	public synchronized boolean add (Dataset d) {
		
		if (this.isFull()) {
			return false;
		}
		Enumeration<String> e = this.data.keys();
		String model="";
		Queue<Dataset> list = null;
		while (e.hasMoreElements()) {
			model = e.nextElement();
			list = this.data.get(model);
			if (!list.offer(d)) {
				return false;
			}			
		}
		notifyAll();
		return true;
	}
	
	public synchronized Dataset getDataset (String model) throws EmptySourceException, InvalidArgumentException {	
		
		Dataset data=null;
		
		if (this.isEmpty()) {
			throw new EmptySourceException("Data set empty!!");   
		}
		
		if (!this.data.containsKey(model)) {
			throw new InvalidArgumentException("Model not defined !");		
		}
		data = this.data.get(model).poll();
		//data.setTag(model);
		notifyAll();
		return (data);
		
	}
	
	public synchronized String nextModel() throws EmptySourceException {
		
		if (this.isEmpty()) {
			throw new EmptySourceException("Dataset empty!!");   
		}		
		Enumeration<String> e = this.data.keys();
		if (this.data.size()==1) {	// Very useful when using only one model
			return (e.nextElement());
		}
		String model="";
		Queue<Dataset> list = null;
		int maxSize=-1;
		int currentSize=0;
		String nextModel="";	
		while (e.hasMoreElements()) {
			model = e.nextElement();
			list = this.data.get(model);
			currentSize = list.size();
			if (currentSize>maxSize) {
				maxSize = currentSize;
				nextModel = model;
			}					
		}		
		return (nextModel); 
		
	}
	
	public synchronized String toString () {
		String result = "Max size: "+this.maxSize;
		
		if (this.data==null) {
			result += "object was not yet properly built." ;
			return (result);
		}
		if (this.models.size()==0) {
			result += "Empty model list." ;
			return (result);
		}
		
		// Model list
		Enumeration<String> e = this.data.keys();
		String nextModel="";	
		Queue<Dataset> list = null;
		String model="";
		while (e.hasMoreElements()) {
			model = e.nextElement();
			list = this.data.get(model);
			result+= "\nModel["+model+"]: \t"+list.size();
		}
		result+= "\n";
		return (result);
	}
}
