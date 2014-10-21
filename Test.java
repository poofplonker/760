import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEventType;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

/**
 * The program compares a neural network with a extra input node of random data
 * with a standard neural network on the Sin(x)Cos(2x) problem.
 *  
 * @author Michael Glenny <michaelglenny@gmail.com>
 * @author Laurence McFarlane <lmcf013@aucklanduni.ac.nz>
 */
public class Test implements LearningEventListener {
	//Constant Parameters for the neural network
	final int NODESINHIDDEN = 3;
	final int INPUTNODES = 1;
	final int OUTPUTNODES = 1;
	final int NUMTESTSPERDATASET = 5;
	final int NUMDATASETS = 3;
	final int FILESIZE = 1000;
	final int TESTINGSIZE = FILESIZE * 10;
	final int MAXITER = 20000;
	final double LEARNINGRATE = 0.1;
	final double MAXERROR = 0.0005;

	//Vars used to output formated stats 
	BufferedWriter bw;
	boolean firstTime = false;
	double firstError;
	int firstIterations;
	double secondError;
	int secondIterations;
	int nonRandomCounter = 0;
	int randomCounter = 0;
	int tieCounter = 0;
	double firstTestError;
	double secondTestError;
	
	/**
	 * Starts the running of the neural network
	 */
	public static void main(String[] args) throws IOException {
		new Test().run("SinCos", "Rand", ".csv", "TestRunOutput.csv", "Summary.csv");
	}
	
	/**
	 * Takes a file of comma separated data and add a random column. Then saves this file.
	 * @param beforeColumn The random data will be inserted before this column.
	 * @param prefix The string prefix for both random and non random data.
	 * @param randomfix The random part of the string. Goes after prefix.
	 * @param postfix The end of the file name for both random and non random data.
	 */
	public static void createRandomDataFile(int beforeColumn, String prefix, String randomfix, String postfix){
		try {
			BufferedReader nonRandomReader = new BufferedReader(new FileReader(prefix+postfix));
			BufferedWriter randomWriter = new BufferedWriter(new FileWriter(prefix+randomfix+postfix));
			
			String inputLine = null;
			while ((inputLine = nonRandomReader.readLine()) != null) {
				int splitIndex = 0;
				for(int i = 0; i < beforeColumn; i++){
					splitIndex = inputLine.indexOf(",", splitIndex+1);
				}
				String output = inputLine.substring(0, splitIndex+1) + (Math.random()*2-1) + inputLine.substring(splitIndex, inputLine.length());
				randomWriter.write(output+"\n");
			}
			
			nonRandomReader.close();
			randomWriter.close();
		} catch (IOException e) {
			System.out.println("Error generating dataset file.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a file with sin(x)cos(2x) and data between 0 and 2pi in the format
	 *  x,sin(x)cos(2x)
	 * @param size the number of steps between 0 and 2pi
	 * @param file The name of the file the data will be stored in.
	 */
	public static void createDataSetFile(int size, String file){
		try {
			BufferedWriter rw = new BufferedWriter(new FileWriter(file));

			double x = 0;
			double step = (2*Math.PI)/size;
			for(int i = 0 ; i < size; i++){
				rw.write(x+","+(Math.sin(x)*Math.cos(2*x))+"\n");
				x += step;
			}
			rw.close();

		} catch (IOException e) {
			System.out.println("Error generating dataset file.");
			e.printStackTrace();
		}
	}

	/**
	 * Runs the neural net experiment multiple times on random data sets and initial weights
	 * and then outputs the summary stats
	 * All data files names of format (prefix)(randfix)?(NumberIter)(postfix)
	 * @throws IOException 
	 */
	public void run(String prefix, String randomfix, String postfix, String outputFile, String outputSumFile) throws IOException {
		
		//Create file streams to stats to
		bw = new BufferedWriter(new FileWriter(outputFile));
		BufferedWriter brsum = new BufferedWriter(new FileWriter(outputSumFile));
		
		//Create the training data set for the non random case
		createDataSetFile(FILESIZE, prefix + FILESIZE + postfix);
		createDataSetFile(TESTINGSIZE, prefix + TESTINGSIZE + postfix);
		
		//Creating the verification files
		createRandomDataFile(1, prefix, randomfix, TESTINGSIZE + postfix);
		DataSet testSetRand = DataSet.createFromFile(prefix + randomfix + TESTINGSIZE + postfix,2,1, ",",false);
		DataSet testSetNonRand = DataSet.createFromFile(prefix + TESTINGSIZE + postfix, 1, 1, ",", false);
		
		//Learn on data sets with different random data in them
		for(int k = 0; k < NUMDATASETS; k++){
			
			//Create the random dataset
			createRandomDataFile(1, prefix, randomfix, FILESIZE + postfix);
			
			//Set initial stats
			randomCounter = nonRandomCounter = tieCounter = 0;
			
			//Create the training sets used to train the data
			DataSet trainingSet = DataSet.createFromFile(prefix + FILESIZE + postfix, 1,1, ",", false);
			DataSet randomTrainingSet = DataSet.createFromFile(prefix + randomfix + FILESIZE + postfix, 2,1,",",false);

			//Learn on the same data set with different initial weights
			for(int j = 0; j < NUMTESTSPERDATASET; j++){
				System.out.println("Running for the (" + k + ", " + j + ") time...");
				
				MultiLayerPerceptron myMlPerceptron;
				
				//Create non-random network
				myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, INPUTNODES, NODESINHIDDEN, OUTPUTNODES);
				BackPropagation learningRule = createLearningRule();
				myMlPerceptron.setLearningRule(learningRule);
				myMlPerceptron.randomizeWeights();
				Double[] initialWeights = myMlPerceptron.getWeights();

				//Learn on non-random network
				myMlPerceptron.learn(trainingSet);
				firstTestError = Test.testNeuralNetwork(myMlPerceptron, testSetNonRand);
				
				//Create random network
				myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, INPUTNODES+1, NODESINHIDDEN, OUTPUTNODES);
				myMlPerceptron.setLearningRule(learningRule);
				myMlPerceptron.randomizeWeights();
				Double[] randomWeights = myMlPerceptron.getWeights();
				
				//Set the weights to the same as in the non-random case
				int newWeightLength = initialWeights.length+NODESINHIDDEN;
				double[] stupidArray = new double[newWeightLength];
				for(int i = 0; i < newWeightLength; i++){
					if(i < NODESINHIDDEN){
						stupidArray[i] = initialWeights[i];
					}else if(i < 2*NODESINHIDDEN){
						stupidArray[i] = randomWeights[i];
					}else{ 
						stupidArray[i] = initialWeights[i-NODESINHIDDEN];
					}
				}
				myMlPerceptron.setWeights(stupidArray);
				
				//Run random network
				myMlPerceptron.learn(randomTrainingSet);
				secondTestError = Test.testNeuralNetwork(myMlPerceptron, testSetRand);
								
				//Determine who wins and output the stats to file
				if(firstIterations < MAXITER && secondIterations == MAXITER && firstTestError < secondTestError){
					bw.write("non-random," );
					nonRandomCounter++;
				}else if(firstIterations == MAXITER && secondIterations < MAXITER && firstTestError > secondTestError){
					bw.write("random," );
					randomCounter++;
				}else{
					bw.write("tie,");
					tieCounter++;
				}
				bw.write(firstIterations + "," +firstError + "," + secondIterations + "," + secondError+","+
						firstTestError+","+secondTestError+"\n");

			}
			
			//Print the results of the data set
			System.out.print("Test finished: ");
			System.out.println("Non-Random: "+ nonRandomCounter+", Random: " + randomCounter + ", Ties: " + tieCounter);
			
			//Write the summary data to summary files
			brsum.write(nonRandomCounter + "," + randomCounter +"," + tieCounter+"\n");
			bw.write("---------------------------------------------\n");
			
			//Flush files
			brsum.flush();
			bw.flush();
		}
		
		//Close writing files
		brsum.close();
		bw.close();

	}
	
	/**
	 * Creates the standard learning rule for backproagation
	 * @return BackPropagation
	 */
	public BackPropagation createLearningRule(){
		BackPropagation learningRule = new BackPropagation();
		learningRule.addListener(this);
		learningRule.setLearningRate(LEARNINGRATE);
		learningRule.setMaxError(MAXERROR);
		learningRule.setMaxIterations(MAXITER);
		learningRule.setBatchMode(false);
		return learningRule;
	}
	
	/**
	 * Returns the cumulative error for a neural net over a data set
	 * @param neuralNet neural network
	 * @param trainingSet test set
	 */
	public static double testNeuralNetwork(NeuralNetwork<BackPropagation> neuralNet, DataSet testSet) {
		double error = 0;
		for(DataSetRow testSetRow : testSet.getRows()) {
			neuralNet.setInput(testSetRow.getInput());
			neuralNet.calculate();
			double[] networkOutput = neuralNet.getOutput();
			double[] desiredOutput = testSetRow.getDesiredOutput();

			for(int i = 0; i < networkOutput.length; i++){
				error += Math.abs(networkOutput[i] - desiredOutput[i]);
			}
			
		}
		return error;
	}

	/**
	 * Handles the learning event each time the neural net learns
	 * Will ignore all events except stop, in which case it stores stats
	 * @param LearningEvent the triggering learning event
	 */
	@Override
	public void handleLearningEvent(LearningEvent event) {
		BackPropagation bp = (BackPropagation)event.getSource();
		if (event.getEventType() == LearningEventType.LEARNING_STOPPED){
			if(firstTime == false){
				firstTime = true;
				firstIterations = bp.getCurrentIteration();
				firstError = bp.getTotalNetworkError();
			}else{
				firstTime = false;
				secondIterations = bp.getCurrentIteration();
				secondError = bp.getTotalNetworkError();
			}
		}
	}    

}
