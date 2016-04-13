import java.util.List;

import weka.associations.Apriori;
import weka.associations.AssociationRule;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class Jaccard
{
	public static void main(String[] args) throws Exception
	{
		Instances dataset = DataSource.read("census.arff");//change census with the provided dataset
		int natt = dataset.numAttributes();
		//We proceed by removing every attribute except A B (suppose 0 and 1)
		Remove r = new Remove();
		r.setAttributeIndices("2-" + (natt-1));
		r.setInputFormat(dataset);
		Instances new_dataset = Filter.useFilter(dataset, r);
		
		//  	    	    supp(A->B)
		//J(A,B) =  ------------------------------- > 0.8
		//           supp(A) + supp(B) - supp(A->B)
		
		//  	    	  4supp(A) + 4supp(B)
		//supp(A->B) =  -----------------------
		//           	          9
		
		//supp(A) is the number of rows with attr A = va (the same holds for supp(B)
		//With this expression we can compute supp(A->B) and use it to set the
		//lowerBoundMinSupport also in weka explorer
		//Obviously all the rules generated are exactly those rules with
		//Jaccard index > 0.8
		
		int indexA = 0;
		int indexB = 0;
		int suppA = 0;
		int suppB = 0;
		for(Instance i : dataset)
		{
			suppA += (i.toString(indexA).equals("valoreA")) ? 1 : 0;
			suppB += (i.toString(indexB).equals("valoreB")) ? 1 : 0;
		}
		double suppAB = (double)(4*suppA + 4*suppB) / (double)9;
		
		Apriori ap = new Apriori();
		ap.setDelta(0.01);
		ap.setLowerBoundMinSupport(suppAB);
		ap.buildAssociations(new_dataset);
		List<AssociationRule> rules = ap.getAssociationRules().getRules();
		for(AssociationRule rule : rules)
			System.out.println(rule);
	}

}
