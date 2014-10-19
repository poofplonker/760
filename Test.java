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
	public static void main(String[] args) throws IOException {
		new Test().run(1,"SinCos1000.csv","SinCosRand1000.csv", "TestRunOutput.txt");
	}

	/**
	 * Runs this sample
	 * @throws IOException 
	 */
	public void run(int inputNodes, String filename, String randomFileName, String outputFile) throws IOException {
		final int NODESINHIDDEN = 3;
		final int INPUTNODES = 1;
		final int OUTPUTNODES = 1;
		br = new BufferedWriter(new FileWriter(outputFile));
		for(int j = 0; j < 100; j++){
			// create training set (logical XOR function)
			DataSet trainingSet = DataSet.createFromFile(filename, 1,1, ",", false);
			DataSet randomTrainingSet = DataSet.createFromFile(randomFileName, 2,1,",",false);
			// create multi layer perceptron
			MultiLayerPerceptron myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, INPUTNODES, NODESINHIDDEN, OUTPUTNODES);
			BackPropagation learningRule = createLearningRule();
			myMlPerceptron.setLearningRule(learningRule);
			Double[] initialWeights = myMlPerceptron.getWeights();

			// learn the training set
			System.out.println("Training neural network for first time...");
			myMlPerceptron.learn(trainingSet);

			myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, INPUTNODES+1, NODESINHIDDEN, OUTPUTNODES);
			myMlPerceptron.setLearningRule(learningRule);
			int newWeightLength = initialWeights.length+NODESINHIDDEN;
			double[] stupidArray = new double[newWeightLength];
			for(int i = 0; i < newWeightLength; i++){
				if(i < NODESINHIDDEN){
					stupidArray[i] = initialWeights[i];
				}
				else if(i < 2*NODESINHIDDEN){
					stupidArray[i] = 0;
				}else{ 
					stupidArray[i] = initialWeights[i-NODESINHIDDEN];
				}

			}
			myMlPerceptron.setWeights(stupidArray);
			myMlPerceptron.learn(randomTrainingSet);
		}

	}
	public BackPropagation createLearningRule(){
		BackPropagation learningRule = new BackPropagation();
		learningRule.addListener(this);
		learningRule.setLearningRate(0.1);
		learningRule.setMaxError(0.001);
		learningRule.setMaxIterations(20000);
		learningRule.setBatchMode(false);
		return learningRule;
	}
	/**
	 * Prints network output for the each element from the specified training set.
	 * @param neuralNet neural network
	 * @param trainingSet training set
	 */
	public static void testNeuralNetwork(NeuralNetwork neuralNet, DataSet testSet) {

		for(DataSetRow testSetRow : testSet.getRows()) {
			neuralNet.setInput(testSetRow.getInput());
			neuralNet.calculate();
			double[] networkOutput = neuralNet.getOutput();

			System.out.print("Input: " + Arrays.toString( testSetRow.getInput() ) );
			System.out.println(" Output: " + Arrays.toString( networkOutput) );
		}
	}

	@Override
	public void handleLearningEvent(LearningEvent event) {
		BackPropagation bp = (BackPropagation)event.getSource();
		if (event.getEventType() == LearningEventType.LEARNING_STOPPED)
			try {
				br.write(bp.getCurrentIteration() + ". iteration : "+ bp.getTotalNetworkError());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}    

}
