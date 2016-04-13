import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;

public class PromotionAgeRange
{
	public static void main(String[] args) throws Exception
	{
		Instances dataset = DataSource.read("census.arff");
		int natt = dataset.numAttributes();
		//Set class attribute
		dataset.setClassIndex(natt - 1);
		
		//Training Set
		Resample r = new Resample();
		r.setSampleSizePercent(60);
		r.setNoReplacement(true);
		r.setInputFormat(dataset);
		Instances train = Filter.useFilter(dataset, r);
		//Test set
		r.setInvertSelection(true);
		r.setInputFormat(dataset);
		Instances test = Filter.useFilter(dataset, r);
		
		int max_offers = 500;	//#offers to be sent
		int[] rich = new int[4]; //array of #rich people for each age_range  (0 = 0_19, 1 = 20_39, ....)
		int numR = 0;	//#Rich people
		double[] perc = new double[4]; //array of %rich people for each age_range  (0 = 0_19, 1 = 20_39, ....)
		int[] off = new int[4]; //array of #offers to be sent for each age_range  (0 = 0_19, 1 = 20_39, ....)
		
		//J48 Classifier
		J48 j = new J48();
		j.buildClassifier(train);
		//Evaluation
		Evaluation eval = new Evaluation(dataset);
		eval.evaluateModel(j, test);
		
		//Populating the arrays
		for(Instance i : test)
		{
			//index is the array index (0 = 0_19, 1 = 20_39, ....)
			//Double.parseDouble(i.toString(1)) transforms the string referred to the age attribute
			//to the corresponding double value
			//Then, dividing the age by 20 (20 = bin size) and by applying the floor function
			//i get the index of the array (that's why i need to convert the string to double)
			int age = Integer.parseInt(i.toString(0));
			if(age > 79)
				continue;
			int index = (int) Math.floor((Double.parseDouble(i.toString(0)))/20);
			rich[index] += (j.distributionForInstance(i)[0] >= 0.5) ? 1 : 0;
			numR += (j.distributionForInstance(i)[0] >= 0.5) ? 1 : 0;
		}
		for(int i = 0; i < 4; i++)
		{
			perc[i] = ((double) rich[i] / (double) numR);
			off[i] = (int) (perc[i] * max_offers);
			System.out.println("Offers to " + i + ": " + off[i]);
		}
		
		//"Sending" the offers..
		//We want to send offers to rich (>50K) customers until we rich the maximum offers available for each age range
		for(Instance i : test)
		{
			int age = Integer.parseInt(i.toString(0));
			if(age > 79)
				continue;
			int index = (int) Math.floor((Double.parseDouble(i.toString(0)))/20);
			String income = i.toString(test.classIndex());
			if(off[index] > 0 && income.equals(">50K"))
			{
				--off[index];
				System.out.println("Offer sent to customer:");
				System.out.println(i.toString());
			}
		}
		
	}
}
