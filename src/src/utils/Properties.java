package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;

import errors.InvalidArgumentException;

public class Properties {
	
	
	protected Hashtable<String, String> values = null;

	
	
	public Properties () {
	
		values = new Hashtable<String, String> ();
		
	}
	
	public Properties(String content) {
		
		String str;
	
		values = new Hashtable<String, String> ();
		
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
		String info[] = str.split("=");
		this.setProperty(info[0].trim(), info[1].trim());
	}
	
	
	public void unsetValues() {
		Enumeration <String> k = this.values.keys();
		
		while (k.hasMoreElements()) {
			this.values.put(k.nextElement(), null);
		}
	}
	
	public String toString ()
	{
		return (this.values.toString());
		
	}
	public String toSQLString (String table) {
		
		return ("INSERT INTO "+table+" "+this.keys()+" VALUES "+this.values());
		
	}
	
	public String keys() {
		
		String result="(";
		 
		
		Enumeration <String> k = this.values.keys();
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
		Enumeration <String> k = this.values.keys();
		while (k.hasMoreElements()) {
			result+="'"+this.values.get(k.nextElement())+"'";
			if (k.hasMoreElements()) {
				result+=",";
			}
		}
		result+=")";		
		return (result);
		
	}
	
	public void setProperty (String key, String value) {
		values.put(key, value);	
	}
	
	public void delProperty (String key) {
		
		if (!this.values.containsKey(key)) {
			new InvalidArgumentException("Undefined property !!");
			return;
		}
		this.values.remove(key);
	}
	
	
	public String getValue (String key) {
		if (!values.containsKey(key)) {
			return null;
		}
		return (values.get(key));
	}
}
