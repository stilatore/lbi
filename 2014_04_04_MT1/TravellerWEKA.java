import weka.classifiers.CostMatrix;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.RemoveByName;

public class Traveller
{
	public static void main(String[] args) throws Exception
	{
		InstanceQuery query = new InstanceQuery();
	    query.setDatabaseURL("jdbc:jtds:sqlserver://apa.di.unipi.it/Foodmart");
	    query.setUsername("");
	    query.setPassword("");
	    query.setQuery("WITH travellers AS("
	    		+ "	SELECT DISTINCT c.customer_id, 'yes' AS traveller "
	    		+ "FROM sales_fact s, customer c, store st "
	    		+ "WHERE s.customer_id = c.customer_id AND s.store_id = st.store_id AND c.city != st.store_city) "
	    		+ "SELECT DISTINCT c.customer_id, city, birthdate, marital_status, yearly_income, gender, "
	    		+ "total_children, num_children_at_home, education, date_accnt_opened, member_card, occupation, "
	    		+ "houseowner, num_cars_owned, CASE WHEN traveller IS NULL THEN 'no' ELSE 'yes' END AS traveller "
	    		+ "FROM customer c LEFT OUTER JOIN  travellers t ON c.customer_id = t.customer_id "
	    		+ "ORDER BY c.customer_id");
	    Instances dataset = query.retrieveInstances();
	    int natt = dataset.numAttributes();
	    //Set class attribute
		dataset.setClassIndex(natt - 1);
		
		//Preprocessing
		RemoveByName rbn = new RemoveByName();
		rbn.setExpression("customer_id");
		rbn.setInputFormat(dataset);
		Instances new_dataset = Filter.useFilter(dataset, rbn);
		rbn = new RemoveByName();
		
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
		System.out.println(j.toString());
		//Evaluation
		Evaluation eval = new Evaluation(new_dataset);
		eval.evaluateModel(j, test);
		System.out.println("Accuracy: " + eval.pctCorrect() + "\n");
		
		//JRip Classifier
		System.out.println("JRip Classifier");
		JRip jr = new JRip();
		jr.buildClassifier(train);
		System.out.println(jr.toString());
		//Evaluation
		eval = new Evaluation(new_dataset);
		eval.evaluateModel(jr, test);
		System.out.println("Accuracy: " + eval.pctCorrect() + "\n");
		
		//SimpleLogistic Classifier
		System.out.println("SimpleLogistic Classifier");
		SimpleLogistic sl = new SimpleLogistic();
		sl.buildClassifier(train);
		System.out.println(sl.toString());
		//Evaluation
		eval = new Evaluation(new_dataset);
		eval.evaluateModel(sl, test);
		System.out.println("Accuracy: " + eval.pctCorrect() + "\n");
		
		//CostSensitive Classifier
		System.out.println("CostSensitive Classifier + JRip");
		CostSensitiveClassifier c = new CostSensitiveClassifier();
		c.setClassifier(jr);
		CostMatrix c_matrix = new CostMatrix(2);
		c_matrix.setElement(0, 0, 0.0);
		c_matrix.setElement(0, 1, 1.0);	
		c_matrix.setElement(1, 0, 10.0);//actual = nonTraveller; predicted = Traveller
		c_matrix.setElement(1, 1, 0.0);
		c.setCostMatrix(c_matrix);
		c.buildClassifier(train);
		System.out.println(c.toString());
		//Evaluation
		eval = new Evaluation(new_dataset);
		eval.evaluateModel(c, test);
		System.out.println("Accuracy: " + eval.pctCorrect() + "\n");
	}
}
