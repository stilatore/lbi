import java.util.List;

import weka.associations.Apriori;
import weka.associations.AssociationRule;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;

public class K5anonimity
{
	public static void main(String[] args) throws Exception
	{
		Instances dataset = DataSource.read("census.arff");//change census with the provided dataset
		int natt = dataset.numAttributes();
		//We proceed by removing every attribute except A B and C (suppose 0,1,2)
		Remove r = new Remove();
		r.setAttributeIndices("3-" + (natt-1));
		r.setInputFormat(dataset);
		Instances new_dataset = Filter.useFilter(dataset, r);
		
		//min_support = 1/|dataset|  max_support = 4/|dataset|
		double min_support = 1 / (double) new_dataset.numInstances();
		double max_support = 4 / (double) new_dataset.numInstances();
		
		Apriori ap = new Apriori();
		ap.setDelta(0.0005);
		ap.setLowerBoundMinSupport(min_support);
		ap.setUpperBoundMinSupport(max_support);
		ap.buildAssociations(new_dataset);
		List<AssociationRule> rules = ap.getAssociationRules().getRules();
		for(AssociationRule rule : rules)
			System.out.println(rule);
		
		//Now we need to raise the support to at least 5/|dataset|
		//we can use resample with a percentage of 500
		Resample rs = new Resample();
		rs.setSampleSizePercent(500);
		rs.setInputFormat(new_dataset);
		new_dataset = Filter.useFilter(new_dataset, rs);
		
		min_support = 1 / (double) new_dataset.numInstances();
		max_support = 4 / (double) new_dataset.numInstances();
		
		ap = new Apriori();
		ap.setDelta(0.0005);
		ap.setLowerBoundMinSupport(min_support);
		ap.setUpperBoundMinSupport(max_support);
		ap.buildAssociations(new_dataset);
		List<AssociationRule> rules2 = ap.getAssociationRules().getRules();
		for(AssociationRule rule : rules2)
			System.out.println(rule);
	}

}
