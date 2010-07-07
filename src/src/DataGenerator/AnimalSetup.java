package DataGenerator;

import java.util.ArrayList;
import org.w3c.dom.*;

import errors.InvalidArgumentException;

/**
 * \page todopage TODO page information
 * 
 * 1- The XML parser of Behavior file should implements a regex search ([0-9]*, [0-9]*, [a-z]*, [A-Z]*) to warning about parser errors
 
 */

/**
 * \page xml_setup_file_format XML setup file format description
 * 
 * To define the setup information to data set builder should be given as input
 * a xml file where the setup information can be found. The information is
 * structured by animal, therefore the description is a list of description how
 * can be found and used the information of a given animal. In this list, can be
 * found nodes (animal) with information from files and nodes with information
 * from database.
 * 
 * <h1>Example 01: XML setup file when using the input from spike train files</h1>
 * 
 * \code <experiment> <animal> <name> ge4 </name> <dataset> <type> file </type>
 * <pathToSpikes path="/home/data/spikes/ge4/01/all"> </<pathToSpikes>
 * <pathToBehavior path="/home/data/ge4_contacts.txt"> </<pathToSpikes>
 * </dataset> <params date="June 03, 2010"> <fileFilter> hp </fileFilter>
 * <fileFilter> s1 </fileFilter> <fileFilter> v1 </fileFilter> <label> ball
 * </label> <label> brush </label> <label> food </label> <label> urchin </label>
 * <binSize> .250 </binSize> <windowWidth> 10 </windowWidth> <totalSamples>
 * 70</totalSamples> <alfa> .80 </alfa> <beta> 2 </beta> </params> </animal>
 * <animal> . . . </experiment> \endcode
 * 
 * 
 * 
 * 
 * 
 * 
 * */

/** \brief Handles the setup information defined in xml file*/
// TODO Generate a page with a xml format descrition.
public class AnimalSetup extends Setup {

	/** Path to xml file where can be found the setup information */
	String confiFile = "";

	/** Dataset type: file or database */
	String datasetType = "";

	/** Path to diretory where can be found the spike train files */
	String pathToSpikes = "";

	/**
	 * Path to file where can be found the animal behavior description in terms
	 * of time labels
	 */
	String pathToBehavior = "";

	/** List of filter to be used */
	ArrayList<String> filters = null;

	/** List of time labels */
	ArrayList<String> labels = null;

	/** Size of bin that should be used */
	double binSize = Double.NaN;

	/** Number of bins of each pattern window */
	int windowWidth = Integer.MAX_VALUE;

	/**
	 * Total number of positive samples used to build the dataset. Based on this
	 * number should be derived other number
	 */
	int totalSamples = Integer.MAX_VALUE;

	/** Proportion of the sample space to be used as train set */
	double alfa = Double.NaN;

	/**
	 * Proportion between number of negative samples and number of positive
	 * samples
	 */
	double beta = Double.NaN;

	/** \todo use \ref Properties to stores these informations */

	public AnimalSetup(Node animal) {

		if (animal.getNodeType() == Node.ELEMENT_NODE) {
			Element animalElement = (Element) animal;

			// Getting animal
			this.name = this.parseSingle(animalElement, "name");
			this.binSize = Double.parseDouble(this.parseSingle(animalElement,
					"binSize"));
			this.alfa = Double.parseDouble(this.parseSingle(animalElement,
					"alfa"));
			this.beta = Integer.parseInt(this
					.parseSingle(animalElement, "beta"));
			this.windowWidth = Integer.parseInt(this.parseSingle(animalElement,
					"windowWidth"));
			this.totalSamples = Integer.parseInt(this.parseSingle(
					animalElement, "totalSamples"));
			this.filters = this.parseList(animalElement, "fileFilter");
			this.labels = this.parseList(animalElement, "label");
			this.parseDataSetInfo(animalElement);
		}
	}

	/**
	 * \brief All important attributes in a better way to print
	 */
	public String toString() {
		String result = "";

		result += "Name: " + this.name;
		result += "\t Number of positive samples: " + this.totalSamples;
		result += "\nSize of bin (s): " + this.binSize;
		result += "\t Window width: " + this.windowWidth;
		result += "\t Alfa: " + this.alfa;
		result += "\t Beta: " + this.beta;
		result += "\nFilters : " + this.filters.toString();
		result += "\tLabels : " + this.labels.toString();
		// if (this.datasetType=="file") {
		result += "\nSpike Train path : " + this.pathToSpikes;
		result += "\nBehavior path : " + this.pathToBehavior;
		// }

		return (result);
	}

	/**
	 * \brief Returns the animal area
	 * 
	 * Normally this information is associated with a anatomic area, ex: S1, V1.
	 * This answer is based on a given \c filter. The current supported areas:
	 * "hp","s1","v1". When the filter is "" returns "all"
	 * 
	 * @return Returns the animal area
	 * */
	public String getArea(String filter) {

		if (filter.equals("")) {
			return ("all");
		}
		// HP or DG ou CA
		if ((filter.startsWith("hp")) || (filter.startsWith("dg"))
				|| (filter.startsWith("ca"))) {
			return ("hp");
		}
		if (filter.startsWith("v1")) {
			return ("v1");
		}
		if (filter.startsWith("s1")) {
			return ("s1");
		}
		return "ukn";
	}

	private String parseSingle(Element animalElement, String name) {
		NodeList nameList = animalElement.getElementsByTagName(name);
		if (nameList.getLength() != 1) {
			System.out
					.println("XML Setup Format error: wrong number of element !!");
			// TODO Tratar com exception
		}
		Element nameElement = (Element) nameList.item(0);
		if (name == "pathToSpikes") {
			System.out.println(nameElement.getAttribute("path"));
		}
		NodeList textFNList = nameElement.getChildNodes();
		return (((Node) textFNList.item(0)).getNodeValue().trim());
	}

	private String parsePath(Element animalElement, String name) {
		NodeList nameList = animalElement.getElementsByTagName(name);
		if (nameList.getLength() != 1) {
			System.out
					.println("XML Setup Format error: wrong number of element !!");
			// TODO Tratar com exception
		}
		Element nameElement = (Element) nameList.item(0);

		return (nameElement.getAttribute("path"));
	}

	private ArrayList<String> parseList(Element animalElement, String name) {
		ArrayList<String> list = new ArrayList<String>();
		Element aE;
		String txt;

		NodeList paramsList = animalElement.getElementsByTagName("params");
		if (paramsList.getLength() != 1) {
			System.out.println("ERROR: wrong number of parameters");
		}
		Element inputParams = (Element) paramsList.item(0);

		NodeList areas = inputParams.getElementsByTagName(name);
		for (int i = 0; i < areas.getLength(); i++) {
			aE = (Element) areas.item(i);
			txt = aE.getFirstChild().getNodeValue();
			list.add(txt.trim());
		}
		return (list);

	}

	private void parseDataSetInfo(Element animalElement) {

		NodeList datasetList = animalElement.getElementsByTagName("dataset");
		if (datasetList.getLength() != 1) {
			System.out.println("ERROR: wrong number of datasets");
		}
		Element dataset = (Element) datasetList.item(0);

		this.datasetType = this.parseSingle(dataset, "type");
		this.pathToSpikes = this.parsePath(dataset, "pathToSpikes");
		this.pathToBehavior = this.parsePath(dataset, "pathToBehavior");

	}

	/** \brief Returns the name of config file 
	 * 
	 *    Normally, all information about the animal data is stored in a xml file. This methods returns
	 *    the name of this xml file.
	 * @return the name of config file 
	 */
	public String getConfiFile() {
		return confiFile;
	}

	/** \brief Returns the dataset type 
	 * 
	 *    This information can be TXT or SQL.
	 * @return the name of config file 
	 */
	public String getDatasetType() {
		return datasetType;
	}

	/**
	 * \brief Returns the path to spikes data
	 * 
	 * Normally, the spikes data are stored in files in a given directory. This
	 * method returns the path of that directoty.
	 * 
	 * @return returns a String with the path of that directoty in which are the spikes data
	 */
	public String getPathToSpikes() {
		return pathToSpikes;
	}

	/** \brief Returns the path to behavior description file
	 * 
	 * Please see \ref behaveDataSpec for more information about the behavior description file.
	 * 
	 * @return the path to behavior description file
	 * */
	public String getPathToBehavior() {
		return pathToBehavior;
	}

	/**
	 * \brief Returns the list of filters to be used
	 * 
	 * It is possible, in config file, define a set of filter to be used in the
	 * dataset building. This method returns the list of filters defined in the config file.
	 * 
	 * @return list of filters defined in the config file
	 */
	public ArrayList<String> getFilters() {
		return filters;
	}

	/** \brief Returns the list of labels to be used
	 * 
	 * It is possible, in config file, define a set of time labels to be used in the
	 * dataset building. This method returns the list of labels defined in the config file.
	 * 
	 * @return list of labels defined in the config file.
	 * */
	public ArrayList<String> getLabels() {
		return labels;
	}

	/**
	 * \brief Returns the size of bins used to build the datasets.
	 * 
	 * The dataset is built based from a spike counting process, and in this
	 * process is used a size of bin to get the counting. This method returns,
	 * in milliseconds, this size of bin that should be used.
	 * 
	 * @return The size of bins.
	 */
	public double getBinSize() {
		return binSize;
	}

	/**
	 * \brief Returns the size of window used to build the dataset.
	 * 
	 * The dataset is built getting the information based from a spike counting process, and in this
	 * process is used a size of bin to get the counting. This method returns,
	 * the number bins used for each neuron.
	 * 
	 * @return the number bins used for each neuron to build the dataset.
	 */
	public int getWindowWidth() {
		return windowWidth;
	}

	/** \brief Returns the total samples to be used in the dataset building
	 * 
	 * 
	 * @return the total samples to be used in the dataset building
	 */
	public int getTotalSamples() {
		return totalSamples;
	}

	/**
	 * \brief Returns the proportion of total samples to used as positive
	 * training samples
	 * 
	 * [number of samples used to train]:= [alfa]*[total samples]
	 * 
	 * @return the proportion of total samples to used as training samples
	 */
	public double getAlfa() {
		return alfa;
	}

	/**
	 * \brief Returns the proportion of total samples to used as negative
	 * samples
	 * 
	 * [total number of negative samples]:= [beta]*[total samples]
	 * 
	 * @return the proportion of total samples to used as negative samples when
	 *         compared with positive samples
	 * */ 
	public double getBeta() {
		return beta;
	}

	/** \brief Informs if filters are valid
	 * 
	 * @return \c true if defined filters are valid, or \c false otherwise
	 */
	public boolean validFilters() {

		if (filters == null) {
			return (false);
		}
		if (filters.size() == 0) {
			return (false);
		}

		return (true);

	}

	/** \brief Informs if labels are valid
	 * 
	 * @return \c true if defined labels are valid, or \c false otherwise
	 */
	public boolean validLabels() {
		if (labels == null) {
			return (false);
		}
		if (labels.size() == 0) {
			return (false);
		}

		return (true);

	}

}
