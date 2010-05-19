package utils;
import java.io.File;
import java.util.ArrayList;

public class TxtSpkHandler implements SpkHandlerI {
	
	String animal="";
	String filter = "";
	String dataPath= "";
	ArrayList<SpikeTrain> neurons = null;
	
	
	public TxtSpkHandler (String dataSourcePath, String filter) {
		this.filter = filter;
		this.dataPath = dataSourcePath;
		this.readSpikes(dataSourcePath, filter);
		// TODO implements
		
	}
	
	public TxtSpkHandler (String dataSourcePath, String filter, double a, double b ) {
		// TODO implements
		if (!this.validateInterval(a, b)) {
		return;
		}
		this.filter = filter;
		this.dataPath = dataSourcePath;
		this.readSpikes(dataSourcePath, filter,a,b);
		
	}
	public String getSourceType() {
		return ("TXT");
	}
	
	public String getAnimalName() {	
		return animal;
	}
	
	
	public SpikeTrain getSpikes(String name) {
		int i=0;
		while ( (this.neurons.get(i++).getName()!=name) && (i<this.neurons.size()) ) ;
		if (this.neurons.get(i).getName()==name) {
			return (this.neurons.get(i));
		}
		// TODO implements
		return ((SpikeTrain) null);
	}
	
	public SpikeTrain getSpikes(String name, double a, double b) {
		// TODO implements
		// TODO implement this method in SpikeTrain.
		return ((SpikeTrain) null);			
	}
	
	public void setFilter(String filter) {
	//	this.filter = filter;
	}
	
	public String getFilter() {
		return (filter);
	}
	
	public ArrayList<String> getNeuronNames() {
		ArrayList<String>  names = new ArrayList<String> ();  
		for (int i=0; i<this.neurons.size(); i++) {
			names.add(this.neurons.get(i).getName());
		}
		return (names);
		
	}
	
	public ArrayList<SpikeTrain> getAllSpikes() {
		return (this.neurons);
		// TODO implements
		
	}
	
	public ArrayList<SpikeTrain> getAllSpikes(double a, double b) {
		// TODO implements
		return ((ArrayList<SpikeTrain>) null);
	}

	
	public int getNumberOfNeurons() {	
		return (neurons.size());
	}
	
	
	public double firstSpike() {
		int num = neurons.size();
		double first = Double.MAX_VALUE;
	
		if (num!=0) {
			for (int i = 0; i < num; i++) {
				if (neurons.get(i).first < first) {
					first = neurons.get(i).first;
				}
			}
			return (first);
		}
		return (-1);
	}
	
	
	
	public double lastSpike() {	
		int num = neurons.size();
		double last = Double.MIN_VALUE;

		if (num!=0) {
			for (int i = 0; i < num; i++) {
				if (neurons.get(i).last > last) {
					last = neurons.get(i).last;
				}
			}
			return (last);
		}
		return (-1);
	}
	
	public String toString () {
		String str = "";
		for (int i=0; i<this.neurons.size(); i++) {
			str = str + this.neurons.get(i).toString();
		}
		return (str);
	}
	private boolean validateInterval(double a, double b){
		
		if ( (a<b) ){
			return true;
		}
		
		return false;
	}
	
	private boolean readSpikes (String path, String filter) {
		
		String dataSourcePath = path+'/'+filter+".txt"; 
		// Gets the list of files in the dataset path
		File dir = new File(path);
		String name[] = dir.list();
		this.neurons = new ArrayList<SpikeTrain>();
		if (name==null) {
			System.out.println ("Error:Problems reading spikes: " + dataSourcePath);
			return false;
		}
		int numberOfNeurons = name.length;
		
		if (numberOfNeurons>0) {

			if (filter.length() > 0) {
				for (int i = 0; i < numberOfNeurons; i++) {
					if (name[i].toLowerCase().startsWith(filter.toLowerCase())) {
						this.neurons.add(new TxtSpikeTrain(path + "/"+name[i]));
					}
				}
			}
			else {
				for (int i = 0; i < numberOfNeurons; i++) {
					this.neurons.add(new TxtSpikeTrain(path + "/"+name[i]));
				}
			}
			return true;
		}
		else {
		
			return false;
		}
	}
	
	private boolean readSpikes (String path, String filter, double a, double b) {
		
		String dataSourcePath = path+'/'+filter+".txt"; 
		// Gets the list of files in the dataset path
		File dir = new File(path);
		String name[] = dir.list();
		this.neurons = new ArrayList<SpikeTrain>();
		if (name==null) {
			System.out.println ("Error:Problems reading spikes: " + dataSourcePath);
			return false;
		}
		int numberOfNeurons = name.length;
		TxtSpikeTrain spikes=null;
		if (numberOfNeurons>0) {
			 
			if (filter.length() > 0) {
				for (int i = 0; i < numberOfNeurons; i++) {
					if (name[i].toLowerCase().startsWith(filter.toLowerCase())) {
						try {
							spikes = new TxtSpikeTrain(path + "/"+name[i],a,b);
							this.neurons.add(spikes);
						} 
						catch (Exception e){
							System.out.println ("Problems creating spike train " + name[i]);
						}
						
					}
				}
			}
			else {
				for (int i = 0; i < numberOfNeurons; i++) {
					try {
						spikes = new TxtSpikeTrain(path + "/"+name[i],a,b);
						this.neurons.add(spikes);
					} 
					catch (Exception e){
						System.out.println ("Problems creating spike train " + name[i]);
					}
				}
			}
			return true;
		}
		else {
		
			return false;
		}
	}

	}
