import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;

public class J48_1000test
{

	public static void main(String[] args) throws Exception
	{
		Instances dataset = DataSource.read("census.arff");
		int natt = dataset.numAttributes();
		dataset.setClassIndex(natt-1);
		//to obtain 1000 as test set size, the % of resample is 100 - 2,047 = 97,953
		//Training Set
		Resample r = new Resample();
		r.setSampleSizePercent(97.953);
		r.setNoReplacement(true);
		r.setInputFormat(dataset);
		Instances train = Filter.useFilter(dataset, r);
		System.out.println("Train set: " + train.numInstances());
		//Test set
		r.setInvertSelection(true);
		r.setInputFormat(dataset);
		Instances test = Filter.useFilter(dataset, r);
		System.out.println("Test set: " + test.numInstances());
		
		//J48 Classifier
		J48 j = new J48();
		j.buildClassifier(train);
		//Evaluation
		Evaluation eval = new Evaluation(dataset);
		eval.evaluateModel(j, test);
		System.out.println(eval.pctCorrect());
		
		for(int i = 0; i <= 10; i++)
		{
			//Training set
			r = new Resample();
			r.setNoReplacement(true);
			r.setSampleSizePercent(80); //ogni nuovo training set è ridotto del 20%
			r.setInputFormat(train);
			train = Filter.useFilter(train, r);
			System.out.println("Training set: " + train.numInstances());
			
			//Classifier
			j = new J48();
			j.buildClassifier(train);
			eval = new Evaluation(dataset);
			eval.evaluateModel(j, test);
			System.out.println(eval.pctCorrect());
		}

	}

}
