import weka.classifiers.CostMatrix;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.RemoveByName;

public class MAE
{
	public static void main(String[] args) throws Exception
	{
		InstanceQuery query = new InstanceQuery();
		//Replace with the dataset provided
	    query.setDatabaseURL("jdbc:jtds:sqlserver://apa.di.unipi.it/lbi");
	    query.setUsername("");
	    query.setPassword("");
	    query.setQuery("SELECT * FROM census");
	    Instances dataset = query.retrieveInstances();
		
		//Preprocessing (remove id)
	    RemoveByName rbn = new RemoveByName();
		rbn.setExpression("id");
		rbn.setInputFormat(dataset);
		Instances new_dataset = Filter.useFilter(dataset, rbn);
		//If the selected class is numeric we need to discretize it or convert it to nominal
		NumericToNominal ntn = new NumericToNominal();
		ntn.setAttributeIndices("first");
		ntn.setInputFormat(new_dataset);
		new_dataset = Filter.useFilter(new_dataset, ntn);
		
		new_dataset.setClassIndex(0); //Since it's an ordinal classification problem we set attribute Age as class
		Attribute class_att = new_dataset.attribute(0);
		int num = class_att.numValues(); //num class values
		
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
		J48 j = new J48();
		j.buildClassifier(train);
		//CostSensitiveClassifier
		CostSensitiveClassifier c = new CostSensitiveClassifier();
		c.setClassifier(j);
		CostMatrix c_matrix = new CostMatrix(num);
		//Now we need to populate the whole matrix with the appropriate costs
		for(int n = 0; n < num; n++)
			for(int m = 0; m < num; m++)
				c_matrix.setElement(n, m, Math.abs(n-m));
		//Example for a 3x3 matrix:
		//	0 1 2
		//	1 0 1
		//	2 1 0
		c.setCostMatrix(c_matrix);
		c.buildClassifier(train);
		//Evaluation
		Evaluation eval = new Evaluation(new_dataset);
		eval.evaluateModel(c, test);
		System.out.println(eval.meanAbsoluteError());
	}
}
