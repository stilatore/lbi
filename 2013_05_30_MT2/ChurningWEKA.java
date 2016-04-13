import java.util.ArrayList;
import java.util.Collections;

import weka.classifiers.CostMatrix;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.RemoveByName;

public class Churning
{

	public static void main(String[] args) throws Exception
	{
		InstanceQuery query = new InstanceQuery();
	    query.setDatabaseURL("jdbc:jtds:sqlserver://apa.di.unipi.it/Foodmart");
	    query.setUsername("");
	    query.setPassword("");
	    query.setQuery(""
	    		+ "WITH prev_month AS ( "
	    		+ "SELECT s.customer_id, t.month_of_year, t.the_month, (t.month_of_year -1) as prev "
	    		+ "FROM sales_fact_1998 s JOIN time_by_day t ON s.time_id = t.time_id ), "
	    		+ "vista AS ( "
	    		+ "SELECT v1.customer_id, v2.customer_id as cust_toCheck, v1.month_of_year, v1.the_month "
	    		+ "FROM prev_month v1 LEFT OUTER JOIN prev_month v2 ON v1.customer_id = v2.customer_id and v1.month_of_year = v2.prev ) "
	    		+ "SELECT v.customer_id, the_month, city, state_province, country, birthdate, marital_status, "
	    		+ "yearly_income, gender, total_children, num_children_at_home, education, date_accnt_opened, "
	    		+ "member_card, occupation, houseowner, num_cars_owned, "
	    		+ "CASE WHEN cust_toCheck IS NULL THEN 'yes' ELSE 'no' END AS churning "
	    		+ "FROM vista v JOIN customer c ON v.customer_id = c.customer_id "
	    		+ "GROUP BY v.customer_id, cust_toCheck, the_month, city, state_province, country, birthdate, marital_status, "
	    		+ "yearly_income, gender, total_children, num_children_at_home, education, date_accnt_opened, "
	    		+ "member_card, occupation, houseowner, num_cars_owned "
	    		+ "ORDER BY v.customer_id, the_month");
	    Instances dataset = query.retrieveInstances();
	    int natt = dataset.numAttributes();
	    //Set class attribute
		dataset.setClassIndex(natt - 1);
		double target_perc = 0.2;
	    
		//Preprocessing
		RemoveByName rbn = new RemoveByName();
		rbn.setExpression("customer_id");
		rbn.setInputFormat(dataset);
		Instances newDataset = Filter.useFilter(dataset, rbn);
		//+ eventuale rimozione del mese di dicembre
		
		//Training Set
		Resample r = new Resample();
		r.setSampleSizePercent(60);
		r.setNoReplacement(true);
		r.setInputFormat(newDataset);
		Instances train = Filter.useFilter(newDataset, r);
		//Test set
		r.setInvertSelection(true);
		r.setInputFormat(newDataset);
		Instances test = Filter.useFilter(newDataset, r);
		
		//J48 Classifier
		System.out.println("J48 Classifier");
		J48 j = new J48();
		j.buildClassifier(train);
		//Evaluation
		Evaluation eval = new Evaluation(newDataset);
		eval.evaluateModel(j, test);
		
		
		//arraylist delle prob
		//soglia = prob dell'istanza di indice index
		//ciclo sul test set e calcolo il recall(tp/tp+fn) solo per il sottoinsieme con la nuova soglia
		//churning 0   non churning 1
		ArrayList<Double> probList = new ArrayList<Double>();
		for(Instance i : test)
			probList.add(j.distributionForInstance(i)[0]);
		Collections.sort(probList);
		Collections.reverse(probList);
		int index = (int)(test.numInstances() * target_perc); //target 20% of test set
		double threshold = probList.get(index);
		int tp = 0;
		int fn = 0;
		for(Instance i : test)
		{
			String churning = i.toString(test.numAttributes() - 1);
			tp += (churning.equals("yes") && j.distributionForInstance(i)[0] >= threshold) ? 1 : 0;
			fn += (churning.equals("yes") && j.distributionForInstance(i)[0] < threshold) ? 1 : 0;
		}
		double recall = (double)tp / ((double)tp + (double)fn);
		System.out.println("Recall: " + recall);
		
		//************************************************************
		
		//JRip Classifier
		System.out.println("JRip Classifier");
		JRip jr = new JRip();
		jr.buildClassifier(train);
		//Evaluation
		eval = new Evaluation(newDataset);
		eval.evaluateModel(jr, test);

		probList = new ArrayList<Double>();
		for(Instance i : test)
			probList.add(jr.distributionForInstance(i)[0]);
		Collections.sort(probList);
		Collections.reverse(probList);
		index = (int)(test.numInstances() * target_perc); //target 20% of test set
		threshold = probList.get(index);
		tp = 0;
		fn = 0;
		for(Instance i : test)
		{
			String churning = i.toString(test.numAttributes() - 1);
			tp += (churning.equals("yes") && jr.distributionForInstance(i)[0] >= threshold) ? 1 : 0;
			fn += (churning.equals("yes") && jr.distributionForInstance(i)[0] < threshold) ? 1 : 0;
		}
		recall = (double)tp / ((double)tp + (double)fn);
		System.out.println("Recall: " + recall);
		
		//************************************************************
		
		//Provo un CostSensitive classifier con JRip per cercare di ottenere un recall >
		//CostSensitive Classifier
		System.out.println("CostSensitive Classifier + JRip");
		CostSensitiveClassifier c = new CostSensitiveClassifier();
		c.setClassifier(jr);
		CostMatrix c_matrix = new CostMatrix(2);
		c_matrix.setElement(0, 0, 0.0);
		c_matrix.setElement(0, 1, 9.0);	//actual = churning; predicted = non churning
										//assegno un peso maggiore (9) in modo da evitare che
										//gli actual churning vengano classificati come non churning
		c_matrix.setElement(1, 0, 1.0); //actual = non churning; predicted = churning
		c_matrix.setElement(1, 1, 0.0);
		c.setCostMatrix(c_matrix);
		c.buildClassifier(train);
		//Evaluation
		eval = new Evaluation(newDataset);
		eval.evaluateModel(c, test);

		probList = new ArrayList<Double>();
		for(Instance i : test)
			probList.add(c.distributionForInstance(i)[0]);
		Collections.sort(probList);
		Collections.reverse(probList);
		index = (int)(test.numInstances() * target_perc); //target 20% of test set
		threshold = probList.get(index);
		tp = 0;
		fn = 0;
		for(Instance i : test)
		{
			String churning = i.toString(test.numAttributes() - 1);
			tp += (churning.equals("yes") && c.distributionForInstance(i)[0] >= threshold) ? 1 : 0;
			fn += (churning.equals("yes") && c.distributionForInstance(i)[0] < threshold) ? 1 : 0;
		}
		recall = (double)tp / ((double)tp + (double)fn);
		System.out.println("Recall: " + recall);
		
		//************************************************************
	}
}
