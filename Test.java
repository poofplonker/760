/**
 * Copyright 2010 Neuroph Project http://neuroph.sourceforge.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEventType;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;

/**
 * This sample shows how to create, train, save and load simple Multi Layer Perceptron for the XOR problem.
 * This sample shows basics of Neuroph API.
 * @author Zoran Sevarac <sevarac@gmail.com>
 */
public class Test implements LearningEventListener {

	BufferedWriter br;
	boolean firstTime = false;
	double firstError;
	int firstIterations;
	int nonRandomCounter = 0;
	int randomCounter = 0;
	int tieCounter = 0;
	double firstTestError;
	double secondTestError;
	final int MAXITER = 20000;
	public static void main(String[] args) throws IOException {
		new Test().run(1,"SinCos1000.csv","SinCosRand1000.csv", "TestRunOutput.txt", "Summary.txt");
	}

	public static void createRandomFile(int size){
		try {
			BufferedWriter rw = new BufferedWriter(new FileWriter("SinCosRand"+size+".csv"));

			double x = 0;
			double step = (2*Math.PI)/size;
			for(int i = 0 ; i < size; i++){
				rw.write(x+","+(Math.random()*2-1)+","+(Math.sin(x)*Math.cos(2*x))+"\n");
				x += step;
			}
			rw.close();

		} catch (IOException e) {
			System.out.println("Error generating new random file.");
			e.printStackTrace();
		}
	}

	/**
	 * Runs this sample
	 * @throws IOException 
	 */
	public void run(int inputNodes, String filename, String randomFileName, String outputFile, String outputSumFile) throws IOException {
		final int NODESINHIDDEN = 3;
		final int INPUTNODES = 1;
		final int OUTPUTNODES = 1;
		final int NUMTESTSPERDATASET = 10;
		final int NUMDATASETS = 2;
		final int FILESIZE = 1000;
		br = new BufferedWriter(new FileWriter(outputFile));
		BufferedWriter brsum = new BufferedWriter(new FileWriter(outputSumFile));
		
		createRandomFile(FILESIZE*10);
		DataSet testSetRand = DataSet.createFromFile("SinCosRand10000.csv",2,1, ",",false);
		DataSet testSetNonRand = DataSet.createFromFile("SinCos10000.csv", 1, 1, ",", false);
		for(int k = 0; k < NUMDATASETS; k++){
			createRandomFile(FILESIZE);
			for(int j = 0; j < NUMTESTSPERDATASET; j++){
				// create training set (logical XOR function)
				DataSet trainingSet = DataSet.createFromFile(filename, 1,1, ",", false);
				DataSet randomTrainingSet = DataSet.createFromFile(randomFileName, 2,1,",",false);
				// create multi layer perceptron
				MultiLayerPerceptron myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, INPUTNODES, NODESINHIDDEN, OUTPUTNODES);
				BackPropagation learningRule = createLearningRule();
				myMlPerceptron.setLearningRule(learningRule);
				myMlPerceptron.randomizeWeights();
				Double[] initialWeights = myMlPerceptron.getWeights();

				// learn the training set
				System.out.println("Training neural network for " + j + " time...");
				myMlPerceptron.learn(trainingSet);
				firstTestError = Test.testNeuralNetwork(myMlPerceptron, testSetNonRand);

				myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, INPUTNODES+1, NODESINHIDDEN, OUTPUTNODES);
				myMlPerceptron.setLearningRule(learningRule);
				myMlPerceptron.randomizeWeights();
				Double[] randomWeights = myMlPerceptron.getWeights();
				
				int newWeightLength = initialWeights.length+NODESINHIDDEN;
				double[] stupidArray = new double[newWeightLength];
				for(int i = 0; i < newWeightLength; i++){
					if(i < NODESINHIDDEN){
						stupidArray[i] = initialWeights[i];
					}
					else if(i < 2*NODESINHIDDEN){
						stupidArray[i] = randomWeights[i];
					}else{ 
						stupidArray[i] = initialWeights[i-NODESINHIDDEN];
					}

				}
				myMlPerceptron.setWeights(stupidArray);
				myMlPerceptron.learn(randomTrainingSet);
				secondTestError = Test.testNeuralNetwork(myMlPerceptron, testSetRand);
				br.write(firstTestError + "," + secondTestError + "\n");
			}
			System.out.println("Test finished:" + nonRandomCounter + " instances of non-random, "
					+ randomCounter + " instances of randomCounter");
			System.out.println("There are " + tieCounter + " ties");
			System.out.println("Number of tests: " + NUMTESTSPERDATASET);
			brsum.write(nonRandomCounter + "," + randomCounter +"," + tieCounter+"\n");
			randomCounter = nonRandomCounter = tieCounter = 0;
			br.write("---------------------------------------------\n");
		}
		brsum.flush();
		brsum.close();
		br.flush();
		br.close();


	}
	public BackPropagation createLearningRule(){
		BackPropagation learningRule = new BackPropagation();
		learningRule.addListener(this);
		learningRule.setLearningRate(0.1);
		learningRule.setMaxError(0.001);
		learningRule.setMaxIterations(MAXITER);
		learningRule.setBatchMode(false);
		return learningRule;
	}
	/**
	 * Prints network output for the each element from the specified training set.
	 * @param neuralNet neural network
	 * @param trainingSet training set
	 */
	public static double testNeuralNetwork(NeuralNetwork neuralNet, DataSet testSet) {
		double error = 0;
		for(DataSetRow testSetRow : testSet.getRows()) {
			neuralNet.setInput(testSetRow.getInput());
			neuralNet.calculate();
			double[] networkOutput = neuralNet.getOutput();
			double[] desiredOutput = testSetRow.getDesiredOutput();

			//System.out.print("Input: " + Arrays.toString( testSetRow.getInput() ) );
			//System.out.println(" Output: " + Arrays.toString( networkOutput) );

			for(int i = 0; i < networkOutput.length; i++){
				//System.out.println("Error: " + Math.abs(networkOutput[i] - desiredOutput[i]));
				error += Math.abs(networkOutput[i] - desiredOutput[i]);
			}
			
		}
		System.out.println("Error for this case: " + error);
		return error;
	}

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
				try{
					if(firstIterations < MAXITER && bp.getCurrentIteration() == MAXITER){
						br.write("non-random," );
						nonRandomCounter++;
					}else if(firstIterations == MAXITER && bp.getCurrentIteration() < MAXITER){
						br.write("random," );
						randomCounter++;
					}else{
						br.write("tie,");
						tieCounter++;
					}
					br.write(firstIterations + "," +firstError + "," + bp.getCurrentIteration() + "," + bp.getTotalNetworkError()+",");
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}    

}
