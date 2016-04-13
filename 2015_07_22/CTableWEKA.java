import java.util.List;

import weka.associations.Apriori;
import weka.associations.AssociationRule;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.SubsetByExpression;

public class CTableWEKA
{
	public static void main(String[] args) throws Exception
	{
		Instances dataset = DataSource.read("census.arff");
		int natt = dataset.numAttributes();
		
		//Preprocessing
		//We procede by removing instances where attribute C = vc
		//A = sex	B = income
		//C = Race    vc = White
		SubsetByExpression sbe = new SubsetByExpression();
		sbe.setExpression("ATT9 is 'White'");
		sbe.setInputFormat(dataset);
		//We procede by removing all attributes except A and B since we don't need them
		Instances new_dataset = Filter.useFilter(dataset, sbe);
		Remove r = new Remove();
		r.setAttributeIndices("1-9,11-14");
		r.setInputFormat(new_dataset);
		new_dataset = Filter.useFilter(new_dataset, r);
		
		//Contingency table
		int a = 0;
		int b = 0;
		int c = 0;
		int d = 0;
		int n1 = 0;
		int n2 = 0;
		int m1 = 0;
		int m2 = 0;
		int n = new_dataset.numInstances();
		
		//int num_attA = new_dataset.attribute(0).numValues();
		//int num_attB = new_dataset.attribute(1).numValues();
		
		//Association rule
		Apriori ap = new Apriori();
		ap.setDelta(0.005);
		ap.setLowerBoundMinSupport(0.001);
		ap.setMinMetric(0.01);
		//ap.setNumRules(num_attA * num_attB);
		ap.buildAssociations(new_dataset);
		//System.out.println(a);
		List<AssociationRule> rules = ap.getAssociationRules().getRules();
		for(AssociationRule rule : rules)
		{
			//System.out.println(rule);
			a = rule.getTotalSupport();
			n1 = rule.getPremiseSupport();
			m1 = rule.getConsequenceSupport();
			b = n1 - a;
			c = m1 - a;
			n2 = n - n1;
			m2 = n - m1;
			d = n2 - c;
			System.out.println("va: " + rule.getPremise().toString() + " " 
			+ "vb: " + rule.getConsequence().toString() + " " + a + " " + b + " " + c + " " + d);
		}
	}

}
