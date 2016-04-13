import java.util.ArrayList;
import java.util.Collections;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;

public class Es28_05_2015
{

	public static void main(String[] args) throws Exception
	{
		Instances dataset = DataSource.read("census.arff");
		int natt = dataset.numAttributes();
		dataset.setClassIndex(natt-1);
	
		//Training set
		Resample r = new Resample();
		r.setSampleSizePercent(60);
		r.setNoReplacement(true);
		r.setInputFormat(dataset);
		Instances train = Filter.useFilter(dataset, r);
		//Test set
		r.setInvertSelection(true);
		r.setInputFormat(dataset);
		Instances test = Filter.useFilter(dataset, r);
		
		//Classifier
		J48 j = new J48();
		j.buildClassifier(train);
		Evaluation eval = new Evaluation(dataset);
		eval.evaluateModel(j, test);
		System.out.println("Pure J48 accuracy: " + eval.pctCorrect());	//Accuracy J48 puro
		
		//TestSet: ArrayList con le prob di essere classificato come >50K di istanze Male e Female
		ArrayList<Double> probRM = new ArrayList<Double>();
		ArrayList<Double> probRF = new ArrayList<Double>();
		int numRich = 0;	//num istanze classificate come >50K
		int numM = 0;		
		int numF = 0;
		int numRM = 0;		//num istanze M&>50K
		int numRF = 0;		//num istanze F&>50K
		for(Instance i : test)
		{
			String sex = i.toString(9);
			String income = i.toString(14);
			if(sex.equals("Male"))
			{
				++numM;
				probRM.add(j.distributionForInstance(i)[0]);
				if(j.distributionForInstance(i)[0] >= 0.5)
				{
					++numRM;
					++numRich;
				}
			}
			else
			{
				++numF;
				probRF.add(j.distributionForInstance(i)[0]);
				if(j.distributionForInstance(i)[0] >= 0.5)
				{
					++numRF;
					++numRich;
				}
			}
		}
		Collections.sort(probRM);
		Collections.reverse(probRM);
		Collections.sort(probRF);
		Collections.reverse(probRF);
		int m,f;
		m =  numRich * numM / (numM + numF); //index
		f = numRich - m; //index		probabili errori con missing values
		Double thresholdM = probRM.get(m - 1);	//valore della probabilita oltre il quale avviene il cambio di classe
		Double thresholdF = probRF.get(f - 1);
		
		int predictedCorrect = 0;
		for(Instance i : test)
		{
			//Se l'istanza e' classificata come >50K con prob >= della soglia
			//oppure se l'istanza è classificata come <50K con prob < della soglia
			//=> ++predictedCorrect
			String sex = i.toString(9);
			String income = i.toString(14);
			Double threshold = (sex.equals("Male")) ? thresholdM : thresholdF;
			if( ((income.equals(">50K")) && (j.distributionForInstance(i)[0] >= threshold)) || ((income.equals("<=50K")) && (j.distributionForInstance(i)[0] < threshold)))
				++predictedCorrect;	
		}
		
		double myAcc = (double)(100*predictedCorrect/test.numInstances());
		System.out.println("My classifier accuracy: " + myAcc);
	}

}
