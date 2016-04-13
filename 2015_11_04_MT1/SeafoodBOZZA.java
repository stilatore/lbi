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
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.RemoveByName;

public class Seafood
{
	public static void main(String[] args) throws Exception
	{
		InstanceQuery query = new InstanceQuery();
	    query.setDatabaseURL("jdbc:jtds:sqlserver://apa.di.unipi.it/Foodmart");
	    query.setUsername("");
	    query.setPassword("");
	    query.setQuery("WITH sea_sales AS "
	    		+ "(SELECT DISTINCT c.customer_id, 'yes' AS seafood_buyer "
	    		+ "FROM customer c, product p, product_class pc, sales_fact s "
	    		+ "WHERE c.customer_id = s.customer_id AND s.product_id = p.product_id "
	    		+ "AND p.product_class_id = pc.product_class_id AND pc.product_department = 'Seafood') "
	    		+ "SELECT s.customer_id, c.city, c.state_province, c.country, c.yearly_income, "
	    		+ "c.gender, c.total_children, c.occupation, c.houseowner, c.num_cars_owned, "
	    		+ "SUM(store_sales) AS total_sales, "
	    		+ "CASE WHEN seafood_buyer IS NULL THEN 'no' ELSE 'yes' END AS seafood_buyer "
	    		+ "FROM sales_fact s LEFT OUTER JOIN sea_sales ss ON s.customer_id = ss.customer_id "
	    		+ "JOIN customer c ON s.customer_id = c.customer_id "
	    		+ "GROUP BY s.customer_id, seafood_buyer, c.city, c.state_province, c.country, c.yearly_income, "
	    		+ "c.gender, c.total_children, c.occupation, c.houseowner, c.num_cars_owned "
	    		+ "ORDER BY s.customer_id");
	    Instances dataset = query.retrieveInstances();
	    int natt = dataset.numAttributes();
	    //Set class attribute seafood_buyer
		dataset.setClassIndex(natt - 1);
		//Con la query SQL seleziono alcuni attributi utili per la classificazione
		//e, in particolare, le total_sales per cliente e se ha acquistato o meno
		//dal reparto seafood(seafood_buyer)
		//Procedo poi con la rimozione dell'attributo customer_id e la discretizzazione
		//dell'attributo total_sales
		//Considero come classe seafood_buyer e suddivido il dataser in training e test set
		//Applico un paio di classificatori (J48 in particolare assegna tutto come seafood_buyer = 'no'
		//Eventualmente costruisco un classificatore CostSensitive attribuendo il giusto
		//peso ai seafood_buyer realmente 'no' ma predetti come 'yes' (difficile che capita)
		
		//Preprocessing
		RemoveByName rbn = new RemoveByName();
		rbn.setExpression("customer_id");
		rbn.setInputFormat(dataset);
		Instances new_dataset = Filter.useFilter(dataset, rbn);
		Discretize d = new Discretize();
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
		c_matrix.setElement(0, 1, 10.0);	
		c_matrix.setElement(1, 0, 1.0);
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
