package utils;

/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    CreateInstances.java
 *    Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 *
 */

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.*;

/**
 * Generates an weka.core.Instances object with different attribute types.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class CreateInstances {

	/**
	 * Generates the Instances object and outputs it in ARFF format to stdout.
	 * 
	 * @param args
	 *            ignored
	 * @throws Exception
	 *             if generation of instances fails
	 */
	public static void main(String[] args) throws Exception {
		FastVector atts;
		FastVector attsRel;
		FastVector attClass;
		FastVector attValsRel;
		Instances data;
		Instances dataRel;
		double[] vals;
		double[] valsRel;
		int i;

		// 1. set up attributes
		atts = new FastVector();
		// - numeric
		for (i = 0; i < 10; i++) {
			atts.addElement(new Attribute("att1_" + i));
		}
		attClass = new FastVector();
		attClass.addElement ("yes");
		attClass.addElement ("no");
			
		
		atts.addElement(new Attribute("class",attClass));
	//	vals[1] = attVals.indexOf("val3");
		// 2. create Instances object
		data = new Instances("objectId", atts, 0);
		vals = new double[data.numAttributes()];
		for (i = 0; i < data.numAttributes()-1; i++) {
			vals[i] = i;
		}
		vals[data.numAttributes()-1] = attClass.indexOf("yes");
		data.add(new Instance(1.0, vals.clone()));
		for (i = 0; i < data.numAttributes()-1; i++) {
			vals[i] = 2*i;
		}
		vals[data.numAttributes()-1] = attClass.indexOf("no");
		data.add(new Instance(1.0, vals));
		data.setClassIndex(data.numAttributes() - 1);

		weka.classifiers.bayes.NaiveBayes cModel = new weka.classifiers.bayes.NaiveBayes();
		
        cModel.buildClassifier(data);

        Evaluation eval = new Evaluation(data);
        eval.evaluateModel(cModel, data);
        System.out.println (eval.weightedAreaUnderROC());
/*
        C.setAUROC(eval.weightedAreaUnderROC());
        C.setFMeasure(eval.fMeasure(0));
        C.setKappa(eval.kappa());
        C.setPctCorrect(eval.pctCorrect());
        C.setEndTime(System.currentTimeMillis()); */


		System.out.println(data);

	}
}
