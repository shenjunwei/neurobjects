package DataGenerator;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;


public class NegativeList {
	
	protected ArrayList<String> list = null;
	
	public NegativeList (Hashtable<String, ArrayList<Integer>> patIdxs, String positiveLabel) {
	
		
		this.list = new ArrayList<String> ();
		this.buildList(patIdxs, positiveLabel);
		
	}
	
	private void buildList (Hashtable<String, ArrayList<Integer>> patIdxs, String positiveLabel) {
		String label;
		int i,num;
		Enumeration <String> e = patIdxs.keys();
		while(e.hasMoreElements()) {
			label = e.nextElement();
			if (!label.equals(positiveLabel)) {
				num = patIdxs.get(label).size();
				for (i=0; i<num; i++) {
					this.list.add(label+":"+i);
				}
			}
		}
	}
	
	/** \todo Defines the legal format to label. Not use ':', for example. */
	public String getLabel (int pos) {
		if (pos>this.list.size()-1) {
			new IllegalArgumentException("\nInvalid position in negative list");
		}
		return (this.list.get(pos).split(":")[0]);
	}
	
	/** \todo Defines the legal format to label. Not use ':', for example. */
	public int getIdx (int pos) {
		if (pos>this.list.size()-1) {
			new IllegalArgumentException("\nInvalid position in negative list");
		}
		return (Integer.parseInt(this.list.get(pos).split(":")[1]));
	}
	
	public String[] getInfo (int pos) {
		if (pos>this.list.size()-1) {
			new IllegalArgumentException("\nInvalid position in negative list: "+pos);
		}
		return (this.list.get(pos).split(":"));
	}
	
	public int size() {
		return (this.list.size());
	}
	
	public void remove(int pos) {
		if ( (pos>this.list.size()-1) || (pos<0) )  {
			new IllegalArgumentException("\nInvalid position in negative list: "+pos);
		}
		this.list.remove(pos);
	}

}
