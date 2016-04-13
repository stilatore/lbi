import java.util.List;

import weka.associations.Apriori;
import weka.associations.AssociationRule;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Es20130911
{
	public static void main(String[] args) throws Exception
	{
		//1)	1,5 * sup(A) <= conf(B->A)
		//
		//				 conf(B->A)
		//		1,5 <= ------------- = lift(B->A)
		//				   sup(A)
		//
		//2)	conf(B->A) <= conf(A->B)
		//
		//		 sup(B->A)     sup(A->B)
		//		---------- <= ----------
		//		  sup(B)		sup(A)
		//
		//			sup(B) >= sup(A)
		
		Instances dataset = DataSource.read("census.arff");
		int natt = dataset.numAttributes();
		
		//Preprocessing
		//...
		
		//Association rule
		Apriori ap = new Apriori();
		ap.setDelta(0.005);
		ap.setLowerBoundMinSupport(0.001);
		ap.setCar(true);
		String[] opt = {"-T 1"};
		ap.setOptions(opt);
		//ap.setMetricType(d); //0 = confidence | 1 = lift | 2 = leverage | 3 = Conviction
		ap.setMinMetric(1.5);
		ap.buildAssociations(dataset);
		List<AssociationRule> rules = ap.getAssociationRules().getRules();
		for(AssociationRule rule : rules)
			if(rule.getPremiseSupport() <= rule.getConsequenceSupport())
				System.out.println(rule);
	}
}
