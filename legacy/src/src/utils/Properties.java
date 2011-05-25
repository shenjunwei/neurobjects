package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;


/** \brief Stores a set of properties */
public class Properties {
	
	
	private Hashtable<String, String> values = null;

	public Properties () {
	
		setValues(new Hashtable<String, String> ());
		
	}
	
	public Properties (Properties info) {
		
		setValues(info.cloneTable());
		
	}
	
	public Hashtable<String, String> cloneTable () {
		Hashtable<String, String> tmp = new Hashtable<String, String> ();
		
		Enumeration <String> k = this.getValues().keys();
		
		while (k.hasMoreElements()) {
			String key = k.nextElement();
			tmp.put(key, this.getValue(key));
		}
		return (tmp);
		
	}
	
	public Properties(String content) {
		
		String str;
	
		setValues(new Hashtable<String, String> ());
		
		BufferedReader reader = new BufferedReader(
				  new StringReader(content));
		
		try {
			while ((str = reader.readLine()) != null) {
				this.lineParser(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void lineParser (String str) {
		if (str.isEmpty()) return;
		String info[] = str.split("=");	
		this.setProperty(info[0].trim(), info[1].trim());
	}
	
	
	public void unsetValues() {
		Enumeration <String> k = this.getValues().keys();
		
		while (k.hasMoreElements()) {
			this.getValues().put(k.nextElement(), null);
		}
	}
	
	public String toString ()
	{
		return (this.getValues().toString());
		
	}
	
	public String toComment (String tagComment) {
		String result="";
		String key="";
		Enumeration <String> k = this.getValues().keys();
		while (k.hasMoreElements()) {
			key = k.nextElement();
			result+=tagComment+key+"="+this.getValue(key)+"\n";
		}		
		return (result);
		
	}
	public String toSQLString () {
		String tableName = "";
		String result = "";
		try {
			this.setNetInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (this.keys().contains("table_name")) {
			tableName = this.getValue("table_name");
			this.delProperty("table_name");
		}
		else {
			new IllegalArgumentException("Undefined table name !!");
			return "";
		}
		String keys =this.keys();
		keys = keys.replace(",", "`,`");
		keys = keys.replace("(", "(`");
		keys = keys.replace(")", "`)");
		result = "INSERT INTO "+tableName+" "+keys+" VALUES "+this.values()+";";
		if (!tableName.isEmpty()) {
			this.setProperty("table_name", tableName);
		}
		return (result);
		
	}
	
	public String toSQLString (String tableName) {
		
		String result = "";
		try {
			this.setNetInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (this.keys().contains("table_name")) {
			tableName = this.getValue("table_name");
			this.delProperty("table_name");
		}
		else {
			new IllegalArgumentException("Undefined table name !!");
			return "";
		}
		String keys =this.keys();
		keys = keys.replace(",", "`,`");
		keys = keys.replace("(", "(`");
		keys = keys.replace(")", "`)");
		result = "INSERT INTO "+tableName+" "+keys+" VALUES "+this.values()+";";
		if (!tableName.isEmpty()) {
			this.setProperty("table_name", tableName);
		}
		return (result);
		
	}
	
	private void setNetInfo() throws Exception{


        InetAddress iAddr = InetAddress.getLocalHost () ;
        this.getValues().put("hostname",iAddr.getHostName());
        this.getValues().put("ip",iAddr.getHostAddress());
        

}
	
	public String keys() {
		
		String result="(";
		 
		
		Enumeration <String> k = this.getValues().keys();
		while (k.hasMoreElements()) {
			result+=k.nextElement();
			if (k.hasMoreElements()) {
				result+=",";
			}
		}
		result+=")";		
		return (result);
		
	}
	
	public String values() {
		
		String result="(";
		Enumeration <String> k = this.getValues().keys();
		while (k.hasMoreElements()) {
			result+="'"+this.getValues().get(k.nextElement())+"'";
			if (k.hasMoreElements()) {
				result+=",";
			}
		}
		result+=")";		
		return (result);
		
	}
	
	public void setProperty (String key, String value) {
		getValues().put(key, value);	
	}
	
	public void delProperty (String key) {
		
		if (!this.getValues().containsKey(key)) {
			new IllegalArgumentException("Undefined property !!");
			return;
		}
		this.getValues().remove(key);
	}
	
	
	public String getValue (String key) {
		if (!getValues().containsKey(key)) {
			return null;
		}
		return (getValues().get(key));
	}

	public void setValues(Hashtable<String, String> values) {
		this.values = values;
	}

	public Hashtable<String, String> getValues() {
		return values;
	}
}
