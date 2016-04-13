import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.RemoveByName;

public class Biclass
{
	public static void main(String[] args) throws Exception
	{
		InstanceQuery query = new InstanceQuery();
	    query.setDatabaseURL("jdbc:jtds:sqlserver://apa.di.unipi.it/lbi");
	    query.setUsername("");
	    query.setPassword("");
	    query.setQuery("SELECT *, sex + '_' + class AS biclass FROM census");
	    Instances dataset = query.retrieveInstances();
	    int natt = dataset.numAttributes();
		dataset.setClassIndex(natt - 1);
	    
	    //Preprocessing (remove id, fnlwgt, sex, class(income); discretize age in 5 bins)
	    RemoveByName rbn = new RemoveByName();
		rbn.setExpression("id");
		rbn.setInputFormat(dataset);
		Instances new_dataset = Filter.useFilter(dataset, rbn);
		rbn = new RemoveByName();
		rbn.setExpression("fnlwgt");
		rbn.setInputFormat(new_dataset);
		new_dataset = Filter.useFilter(new_dataset, rbn);
		rbn = new RemoveByName();
		rbn.setExpression("sex");
		rbn.setInputFormat(new_dataset);
		new_dataset = Filter.useFilter(new_dataset, rbn);
		rbn = new RemoveByName();
		rbn.setExpression("class");
		rbn.setInputFormat(new_dataset);
		new_dataset = Filter.useFilter(new_dataset, rbn);
		Discretize d = new Discretize();
		d.setAttributeIndices("first");
		d.setBins(5);
		d.setUseEqualFrequency(true);
		d.setInputFormat(new_dataset);
		new_dataset = Filter.useFilter(new_dataset, d);
		
		//Training Set
		Resample r = new Resample();
		r.setSampleSizePercent(60);
		r.setNoReplacement(true);
		r.setInputFormat(new_dataset);
		Instances train = Filter.useFilter(new_dataset, r);
		//Test set
		r.setInvertSelection(true);
		r.setInputFormat(new_dataset);
		Instances test = Filter.useFilter(new_dataset, r);
		
		//J48 Classifier
		System.out.println("J48 Classifier");
		J48 j = new J48();
		j.buildClassifier(train);
		//Evaluation
		Evaluation eval = new Evaluation(new_dataset);
		eval.evaluateModel(j, test);
		System.out.println(j.toString());
		System.out.println("Accuracy" + eval.pctCorrect());
		
	}

}
