import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;

public class Esercizio3
{
	public static void main(String[] args) throws Exception
	{
		Instances dataset = DataSource.read("census.arff");
		int natt = dataset.numAttributes();
		dataset.setClassIndex(natt-1);
		
        /*
            Il problema in esame richiedeva di verificare se per ogni S1,S2 sottoinsiemi di attributi di S
                (con S1 sottoinsieme di S2) vale che l’accuracy del classificatore J48 calcolata su S1
                è minore dell’accuracy calcolata su S2.
            Non sempre questa affermazione è valida! (Rimuovendo fnlwgt si ottiene un aumento dell’accuracy)
            In generale invece vale il seguente ragionamento:
            Dato l’albero decisionale costruito a partire dall’insieme S procedo rimuovendo gli attributi
                per cui ho un primo split a partire dalla radice;
                saranno questi, quindi, gli attributi che influenzeranno maggiormente la classificazione.
            Proseguo in maniera analoga a partire dal nuovo albero decisionale e osservo che l’accuracy diminuisce.
            È possibile iterare questo procedimento finchè l’albero decisionale è ridotto ad un’unica foglia
                e quindi la classe predetta è scelta  come la classe con più alta numerosità nel training set.
        */
        
		//STEP 1: considero tutti gli attributi (insieme S)
		//Training set
		System.out.println("Classificatore con insieme S");
		Resample r = new Resample();
		r.setSampleSizePercent(60);
		r.setNoReplacement(true);
		r.setInputFormat(dataset);
		Instances train = Filter.useFilter(dataset, r);
		//Test set
		r.setInvertSelection(true);
		r.setInputFormat(dataset);
		Instances test = Filter.useFilter(dataset, r);
		//Classifier
		J48 j = new J48();
		j.buildClassifier(train);
		Evaluation eval = new Evaluation(dataset);
		eval.evaluateModel(j, test);
		//System.out.println(j.toString());  //rimuovere commento per stampare l'albero decisionale
		System.out.println("Accuracy(S): " + eval.pctCorrect());
		System.out.println("----------------------------------------");
		
		
		//STEP 2: rimuovo capital-gain age workclass occupation
		System.out.println("Classificatore con insieme S2");
		Remove rem = new Remove();
		rem.setAttributeIndices("1,2,7,11");
		rem.setInputFormat(dataset);
		Instances new_dataset = Filter.useFilter(dataset, rem);
		//Training set
		r = new Resample();
		r.setSampleSizePercent(60);
		r.setNoReplacement(true);
		r.setInputFormat(new_dataset);
		train = Filter.useFilter(new_dataset, r);
		//Test set
		r.setInvertSelection(true);
		r.setInputFormat(new_dataset);
		test = Filter.useFilter(new_dataset, r);
		//Classifier
		j = new J48();
		j.buildClassifier(train);
		eval = new Evaluation(new_dataset);
		eval.evaluateModel(j, test);
		//System.out.println(j.toString()); //rimuovere commento per stampare l'albero decisionale
		System.out.println("Accuracy(S2): " + eval.pctCorrect());
		System.out.println("----------------------------------------");
		
		
		//STEP 3: rimuovo in aggiunta ai precedenti education,marital status,capital loss, education num e sex
		System.out.println("Classificatore con insieme S1");
		rem = new Remove();
		rem.setAttributeIndices("1,2,4,5,6,7,10,11,12");
		rem.setInputFormat(dataset);
		new_dataset = Filter.useFilter(dataset, rem);
		//Training set
		r = new Resample();
		r.setSampleSizePercent(60);
		r.setNoReplacement(true);
		r.setInputFormat(new_dataset);
		train = Filter.useFilter(new_dataset, r);
		//Test set
		r.setInvertSelection(true);
		r.setInputFormat(new_dataset);
		test = Filter.useFilter(new_dataset, r);
		//Classifier
		j = new J48();
		j.buildClassifier(train);
		eval = new Evaluation(new_dataset);
		eval.evaluateModel(j, test);
		//System.out.println(j.toString());  //rimuovere commento per stampare l'albero decisionale
		System.out.println("Accuracy(S1): " + eval.pctCorrect());
		System.out.println("----------------------------------------");
		
		
		//STEP 4: rimuovo la maggiorparte degli attributi (un'unica foglia nell'albero)
		System.out.println("Classificatore con insieme S0");
		rem = new Remove();
		rem.setAttributeIndices("1,2,4,5,6,7,9,10,11,12,13,14");
		rem.setInputFormat(dataset);
		new_dataset = Filter.useFilter(dataset, rem);
		//Training set
		r = new Resample();
		r.setSampleSizePercent(60);
		r.setNoReplacement(true);
		r.setInputFormat(new_dataset);
		train = Filter.useFilter(new_dataset, r);
		//Test set
		r.setInvertSelection(true);
		r.setInputFormat(new_dataset);
		test = Filter.useFilter(new_dataset, r);
		//Classifier
		j = new J48();
		j.buildClassifier(train);
		eval = new Evaluation(new_dataset);
		eval.evaluateModel(j, test);
		//System.out.println(j.toString());  //rimuovere commento per stampare l'albero decisionale
		System.out.println("Accuracy(S0): " + eval.pctCorrect());
		System.out.println("----------------------------------------");
		
	}

}
