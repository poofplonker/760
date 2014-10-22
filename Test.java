import java.io.BufferedWriter;
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
	static int NODESINHIDDEN = 3;
	static final int INPUTNODES = 1;
	static final int OUTPUTNODES = 1;
	static int NUMTESTSPERDATASET = 5;
	static int NUMDATASETS = 3;
	static int MAXITER = 20000;
	static double LEARNINGRATE = 0.1;
	static double MAXERROR = 0.0005;

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
		String outputFile = "TestRunOutput.csv";
		String summaryFile = "Summary.csv";
		String learnFile = "SinCos1000.csv";
		String learnRandFile = "SinCosRand1000.csv";
		String testFile = "SinCos10000.csv";
		String testRandFile = "SinCosRand10000.csv";
		for(int i = 0; i < args.length; i += 2){
			switch(args[i]){
			case "-nodesinhidden":
				NODESINHIDDEN = Integer.parseInt(args[i+1]);
				break;
			case "-numtestsperdataset":
				NUMTESTSPERDATASET = Integer.parseInt(args[i+1]);
				break;
			case "-numdatasets":
				NUMDATASETS = Integer.parseInt(args[i+1]);
				break;
			case "-maxiter":
				MAXITER = Integer.parseInt(args[i+1]);
				break;
			case "-learningrate":
				LEARNINGRATE = Double.parseDouble(args[i+1]);
				break;
			case "-maxerror":
				MAXERROR = Double.parseDouble(args[i+1]);
				break;
			case "-outputfile":
				outputFile = args[i+1].trim();
				break;
			case "-summaryfile":
				summaryFile = args[i+1].trim();
				break;
			case "-learnfile":
				learnFile = args[i+1].trim();
				break;
			case "-learnrandfile":
				learnRandFile = args[i+1].trim();
				break;
			case "-testfile":
				testFile = args[i+1].trim();
				break;
			case "-testrandfile":
				testRandFile = args[i+1].trim();
				break;
			default:
				System.out.println("Did not recognise the command \""+args[i].trim()+"\".");
			}
		}

		new Test().run(learnFile, learnRandFile, testFile, testRandFile, outputFile, summaryFile);
	}
	

	/**
	 * Runs the neural net experiment multiple times on random data sets and initial weights
	 * and then outputs the summary stats
	 * All data files names of format (prefix)(randfix)?(NumberIter)(postfix)
	 * @throws IOException 
	 */
	public void run(String learnFile, String learnRandFile, String testFile, String testRandFile, String outputFile, String outputSumFile) throws IOException {
		
		//Create file streams to stats to
		bw = new BufferedWriter(new FileWriter(outputFile));
		BufferedWriter brsum = new BufferedWriter(new FileWriter(outputSumFile));
				
		//Creating the verification files
		DataSet testSetRand = DataSet.createFromFile(testRandFile,2,1, ",",false);
		DataSet testSetNonRand = DataSet.createFromFile(testFile, 1, 1, ",", false);
		
		//Learn on data sets with different random data in them
		for(int k = 0; k < NUMDATASETS; k++){
			
			//Create the random dataset
			FileCreator.createExtraRandomDataFile(1, learnFile, learnRandFile);
			
			//Set initial stats
			randomCounter = nonRandomCounter = tieCounter = 0;
			
			//Create the training sets used to train the data
			DataSet trainingSet = DataSet.createFromFile(learnFile, 1,1, ",", false);
			DataSet randomTrainingSet = DataSet.createFromFile(learnRandFile, 2,1,",",false);

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
