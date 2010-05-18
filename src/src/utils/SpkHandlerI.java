package utils;

import java.util.ArrayList;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * \brief Handles spikes information from a give animal
 * 
 * This component should be provide the interface between the spike trains data
 * and the application. The spike trains data is modeled as set of a crescent
 * time series, one crescent time series for each neuron. Besides return the
 * spike trains information, this component provides basic operation as, for
 * example, filters to select neuron names patterns; with these filters it is
 * possible handle spikes from specific areas/electrodes
 * 
 * This interface can be implemented for each mode in which the spike trains
 * data is found: text file, sql database, matlab file.
 * 
 * @author Nivaldo Vasconcelos
 * @date 17May2010
 * 
 * */
public interface SpkHandlerI {

	/**
	 * \brief Returns the type of data source
	 * 
	 * This component should be implemented for each mode in which the spike
	 * trains data is found: text file, sql database, matlab file. This method
	 * returns the current type of the data source.
	 * 
	 * @return the current type of the data source. These values can be: 
	 * 		- \c TXT : when the spike trains used are stored in text files; 
	 * 		- \c SQL : when the spike trains used are stored in a SQL database; 
	 * 		- \c MAT : when the spike trains used are stored in matlab files; 
	 * 		- \c null string if there is no type of data source defined.
	 * 
	 * */
	public String getSourceType();

	/**
	 * \brief Returns the data source animal name.
	 * 
	 * Each one instance of these components should be implemented to handle
	 * spikes from only one animal per time, the name of the current handled
	 * animal is returned by this method.
	 * 
	 * @return the data source animal name, \b or a \c null string if there is
	 *         no animal name defined.
	 * 
	 * */
	public String getAnimalName();

	/**
	 * \brief Returns a spike train as a time series given its name.
	 * 
	 * Given a neuron name, this method returns a set of spike times from that
	 * neuron as a time series. This neuron name is under filter definition,
	 * therefore the given neuron name is seek from a list under filter
	 * selection, if it is not found, this method returns a \c null value as
	 * result.
	 * 
	 * @return the spike train. If the neuron name is not found under
	 *         filter selection returns a \c null value.
	 * 
	 *         \sa setFilter, getFilter
	 * */
	public SpikeTrain getSpikes(String name);

	/**
	 * \brief Returns a spike train as a time series, into a given
	 * time interval.
	 * 
	 * Given a neuron name, and a time interval I=[a;b], this method returns a
	 * set of spike times whose belong to I, from that neuron as a time series.
	 * This neuron name is under filter definition, therefore the given neuron
	 * name is seek from a list under filter selection, if it is not found, this
	 * method returns a \c null value as result.
	 * 
	 * @return the spike train. If the neuron name is not found under filter
	 *         selection returns a \c null value.
	 * 
	 *         \sa setFilter(), getFilter()
	 * */
	public SpikeTrain getSpikes(String name, double a, double b);

	/**
	 * \brief Set the selection filter to neuron names patterns.
	 * 
	 * Given a string pattern, uses this pattern to select neuron names
	 * generating a list of neurons which match with the given string pattern.
	 * After defined this selection all get spike operations under the component
	 * will be stay under this selection. This method is useful, for example, to
	 * select a set of neurons from a same anatomic area/electrode.
	 * 
	 * \code c.setFilter("S1*"); \endcode
	 * 
	 * selects all spike trains whose their names begins with S1. \n
	 * 
	 * \code c.setFilter("S1_01*"); \endcode
	 * 
	 * selects all spike trains whose their names begins with S1_01, and
	 * therefore select all spike trains from a same electrode (S1_01).
	 * 
	 * \code c.setFilter("S1_01a"); \endcode
	 * 
	 * selects the spike train from the neuron S1_01a.
	 */
	public void setFilter(String filter);
	
	/**
	 * \brief Returns current selection filter to neuron names patterns.
	 * 
	 * This a useful to use in UI interfaces to show the current selection
	 * filter.
	 * 
	 * @return the string pattern used to be the current filter \b or a \c null
	 *         string if there is no selected filter.
	 *         
	 *\sa setFilter.
	 * 
	 */
	public String getFilter();
	
	
	/**
	 * \brief Returns current list of neuron names.
	 * 
	 * This a useful to use in UI interfaces to show the current selected
	 * neurons from the selection filter.
	 * 
	 * \sa setFilter.
	 * 
	 * @return a list of neuron names \b or a \c null string if there is no
	 *         selected filter.
	 */
	public ArrayList<String> getNeuronNames();

	/**
	 * \brief Returns a list of all spike trains.
	 * 
	 * Following the current filter selection, returns all spike trains for each
	 * neuron as a list of spike trains. Each spike train is modeled as 1D vector.
	 * 
	 * @return a list of 1D vector, each one as a neuron
	 * 
	 *  \sa setFilter
	 */
	public ArrayList<SpikeTrain> getAllSpikes();

	public ArrayList<SpikeTrain> getAllSpikes(double a, double b);

	
	public int getNumberOfNeurons();

}
