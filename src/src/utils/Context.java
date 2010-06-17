package utils;

import java.net.*;


import java.util.Formatter;
import java.util.Locale;
import java.util.Calendar;
import java.text.SimpleDateFormat;



public class Context {
	
	private		String	model="";
	private 	long	startTime=0;
	private 	long	endTime=0;
	private		double	duration=0;
	private 	String 	animal="";
	private 	String 	object="";
	private 	String 	area="";
	private 	double 	binSize=0;
	private 	int 	windowWidth=0;
	private 	String 	hostname="";
	private 	String 	ip="";
	private 	String 	time="";
	
	private 	double AUROC=0;
	private 	double fMeasure=0;
	private 	double kappa=0;
	private 	double pctCorrect=0;
	private 	String tableName;
	private 	String lasSQLQuery="";
	
	
	public Context (Dataset data, String model) {
		
		this.model = model;
		this.area = data.getArea();
		this.object = data.getLabel();
		// save the current time in mille seconds
		this.startTime = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    this.time=sdf.format(cal.getTime());
	    this.animal = data.getAnimal();
	    this.binSize = data.getBinSize();
	    this.windowWidth = data.getWindowWidth();
	    try {
			this.setNetInfo();
		}
		catch (Exception ex)
		{
			System.out.println("\nProblems to get network information: "+ex.getMessage());
			ex.printStackTrace();
		}
	}

		
	public String getModel() {
		return model;
	}


	private void setNetInfo() throws Exception{

		
		InetAddress iAddr = InetAddress.getLocalHost () ;
		this.hostname = iAddr.getHostName();
		this.ip = iAddr.getHostAddress();
		
	}
	
	public void show () {
		Locale loc=Locale.US;

		System.out.println("Animal: "+this.animal);
		System.out.println("Object: "+this.object);
		System.out.println("Area: "+this.area);
		System.out.println("Bin Size(ms): "+this.binSize);
		System.out.println("Window width: "+this.windowWidth);
		System.out.println("Hostname: "+this.hostname);
		System.out.println("IP: "+this.ip);
		System.out.println("Begin time: "+this.time);
		
		
		System.out.println("Duration (s): "+ new PrintfFormat(loc,"%2.4f").sprintf(this.duration));
		System.out.println("Area under ROC: "+new PrintfFormat(loc,"%2.4f").sprintf(this.AUROC));
		
	}
	
	public void showSQL (String tableName) {
		
		Locale loc=Locale.US;	
		String query = "INSERT INTO " +tableName + " (area,animal,object,bin_size, window_size,model,auroc,fmeasure,kappa,pctcorrect,duration,hostname,ip,status,time) "+
		                "VALUES (\'"+this.area+"\',\'"+this.animal+"\',\'"+this.object+"\',\'"+this.binSize+"\',\'"+this.windowWidth+"\',\'"+
		                         this.model+"\',\'"+new PrintfFormat(loc,"%2.4f").sprintf(this.AUROC)+"\',\'"+
		                         new PrintfFormat(loc,"%2.4f").sprintf(this.fMeasure)+"\',\'"+
		                         new PrintfFormat(loc,"%2.4f").sprintf(this.kappa)+"\',\'"+
		                         new PrintfFormat(loc,"%2.4f").sprintf(this.pctCorrect)+"\',\'"+
		                         new PrintfFormat(loc,"%2.4f").sprintf(this.duration)+"\',\'"+this.hostname+"\',\'"+this.ip+"\',\'"+"OK"+"\',\'"+this.time+"\');";
		
		System.out.println(query);
	}
	
	public String resultSQLQuery() {
		
		if (this.tableName=="") {
			return ("");
		}
				
		Locale loc=Locale.US;	
		String query = "INSERT INTO " +this.tableName + " (area,animal,object,bin_size, window_size,model,auroc,fmeasure,kappa,pctcorrect,duration,hostname,ip,status,time) "+
		                "VALUES (\'"+this.area+"\',\'"+this.animal+"\',\'"+this.object+"\',\'"+this.binSize+"\',\'"+this.windowWidth+"\',\'"+
		                         this.model+"\',\'"+new PrintfFormat(loc,"%2.4f").sprintf(this.AUROC)+"\',\'"+
		                         new PrintfFormat(loc,"%2.4f").sprintf(this.fMeasure)+"\',\'"+
		                         new PrintfFormat(loc,"%2.4f").sprintf(this.kappa)+"\',\'"+
		                         new PrintfFormat(loc,"%2.4f").sprintf(this.pctCorrect)+"\',\'"+
		                         new PrintfFormat(loc,"%2.4f").sprintf(this.duration)+"\',\'"+this.hostname+"\',\'"+this.ip+"\',\'"+"OK"+"\',\'"+this.time+"\');";
		
		
		this.lasSQLQuery = query;
		return (query);
		
	}

	

	public String getAnimal() {
		return animal;
	}


	public String getArea() {
		return area;
	}


	public double getBinSize() {
		return binSize;
	}


	public String getObject() {
		return object;
	}


	public int getWindowWidth() {
		return windowWidth;
	}

	public String getHostname() {
		return hostname;
	}

	public String getIp() {
		return ip;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
		this.duration = (double) (this.endTime-this.startTime)/1000.0;
	}

	public void setAUROC(double auroc) {
		AUROC = auroc;
	}

	public void setFMeasure(double measure) {
		fMeasure = measure;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}

	public void setPctCorrect(double pctCorrect) {
		this.pctCorrect = pctCorrect;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
