package de.uniba.cogsys.bayes;

import java.awt.event.TextEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NaiveBayesClassifier {

	private int indexTargetAttribute = -1;
	private int[] remainingAttributes;
	private String[] allAttributes;
	private List<String[]> trainingExamples = new ArrayList<String[]>();
	private List<String[]> testExamples = new ArrayList<String[]>();
	private Set<String> targetAttributeValues = new HashSet<String>();
	private int n;
	private String positive; // name of positive target class value
	private String negative; // name of positive target class value
	private Map<String, Double> targetValuesFrequency;
	private List<Map<String, Double>> attributeFrequencies = new ArrayList<Map<String, Double>>();
	private List<Map<String, Double>> positiveAttributeFrequencies = new ArrayList<Map<String, Double>>();
	private List<Map<String, Double>> negativeAttributeFrequencies = new ArrayList<Map<String, Double>>();

	private void parseTrainingCSV(String input, String targetAttribute) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line = reader.readLine();

		// read header
		allAttributes = line.split(",");

		// all attributes except class
		remainingAttributes = new int[allAttributes.length - 1];
		int pos = 0;

		for (int i = 0; i < allAttributes.length; i++) {
			if (allAttributes[i].equals(targetAttribute)) {
				// save index of class attribute
				indexTargetAttribute = i;
			} else {
				// otherwise add the attribute to the array of attributes
				remainingAttributes[pos++] = i;
			}
		}

		// read training examples
		while (((line = reader.readLine()) != null)) {
			String[] lineSplit = line.split(",");
			trainingExamples.add(lineSplit);
			targetAttributeValues.add(lineSplit[indexTargetAttribute]);
		}
		reader.close(); // close input file

	}

	public void training(String input, String targetAttribute, String positive, String negative) throws IOException {
		try {
			parseTrainingCSV(input, targetAttribute);
		} catch (IOException e) {
			throw new IOException("Something went wrong while parsing CSV", e);
		}

		n = trainingExamples.size();
		this.positive = positive;
		this.negative = negative;

		targetValuesFrequency = calculateFrequencyOfTarget(trainingExamples, indexTargetAttribute);

		for (int j = 0; j < remainingAttributes.length; j++) {
			attributeFrequencies.add(calculateFrequencyOfTarget(trainingExamples, remainingAttributes[j]));
			positiveAttributeFrequencies
					.add(calculateFrequencyOfAttributeValues(trainingExamples, remainingAttributes[j], positive));
			negativeAttributeFrequencies
					.add(calculateFrequencyOfAttributeValues(trainingExamples, remainingAttributes[j], negative));
		}

		System.out.println("Class Frequency: " + targetValuesFrequency);
		System.out.println(targetValuesFrequency.get("Yes"));

		for (Map.Entry<String, Double> entry : targetValuesFrequency.entrySet()) {
			System.out.println("P(" + entry.getKey() + "): " + entry.getValue() / n);
		}

		System.out.println("Attribute Frequencies: " + attributeFrequencies);
		for (Map<String, Double> attributeFrequency : attributeFrequencies) {
			for (Map.Entry<String, Double> entry : attributeFrequency.entrySet()) {
				System.out.println("P(" + entry.getKey() + "): " + entry.getValue() / n);

			}
		}

		System.out.println("positive Frequencies:" + positiveAttributeFrequencies);
		System.out.println("negative Frequencies:" + negativeAttributeFrequencies);

		System.out.println("Training complete!");
	}

	private Map<String, Double> calculateFrequencyOfTarget(List<String[]> trainingExamples, int indexAttribute) {

		Map<String, Double> targetValuesFrequency = new HashMap<String, Double>();

		for (String[] instance : trainingExamples) {

			String targetValue = instance[indexAttribute];

			if (targetValuesFrequency.get(targetValue) == null) {
				targetValuesFrequency.put(targetValue, 1.0); // increment frequency by 1
			} else {
				targetValuesFrequency.put(targetValue, targetValuesFrequency.get(targetValue) + 1);
			}
		}

		return targetValuesFrequency;
	}

	private Map<String, Double> calculateFrequencyOfAttributeValues(List<String[]> trainingExamples, int indexAttribute,
			String targetAttribute) {
		Map<String, Double> targetValuesFrequency = new HashMap<String, Double>();
		for (String[] instance : trainingExamples) {
			String targetValue = instance[indexAttribute];
			if (instance[indexTargetAttribute].equals(targetAttribute)) {
				if (targetValuesFrequency.get(targetValue) == null) {
					targetValuesFrequency.put(targetValue, 1.0); // increment frequency by 1
				} else {
					targetValuesFrequency.put(targetValue, targetValuesFrequency.get(targetValue) + 1);
				}
			}
		}
		return targetValuesFrequency;
	}

	/**
	 * Based on the calculation of probabilities in {@code training()} this method
	 * predicts the class of the given test file and calculates the metrics by
	 * counting the true positives, false neg. etc..
	 * 
	 * @param input
	 * @throws IOException
	 */
	public void testData(String input) throws IOException {

		System.out.println("Starting to test " + input.toString() + " ...");
		parseTestCSV(input);

		double[] classifications = new double[4]; // 0 = true positive; 1 = false negative; 2 = false positive; 3 = true
													// negative

		for (String[] testExample : testExamples) {
			double sumPos = targetValuesFrequency.get(positive) / n;
			double sumNeg = targetValuesFrequency.get(negative) / n;

			int j = 1;
			for (int i = 0; i < remainingAttributes.length; i++) {

//				System.out.println(testExample[j]);
//				System.out.println(positiveAttributeFrequencies.get(i));

				if (positiveAttributeFrequencies.get(i).get(testExample[j]) != null) {
					sumPos *= positiveAttributeFrequencies.get(i).get(testExample[j])
							/ targetValuesFrequency.get(positive);
				}
				if (negativeAttributeFrequencies.get(i).get(testExample[j]) != null) {
					sumNeg *= negativeAttributeFrequencies.get(i).get(testExample[j])
							/ targetValuesFrequency.get(negative);
				}
				j++;
			}
			System.out.println("P(yes) = " + sumPos);
			System.out.println("P(no) = " + sumNeg);
			if (sumPos > sumNeg) {
				System.out.println("positive prediction");
				if (testExample[indexTargetAttribute].equals(positive)) {
					classifications[0]++;
				} else {
					classifications[2]++;
				}
			} else {
				System.out.println("negative prediction");
				if (testExample[indexTargetAttribute].equals(negative)) {
					classifications[3]++;
				} else {
					classifications[1]++;
				}
			}
		}

		System.out.println("Testing complete! Printing Metrics ...");
		System.out.println("True Positives: " + classifications[0]);
		System.out.println("False Negatives: " + classifications[1]);
		System.out.println("False Positives: " + classifications[2]);
		System.out.println("True Negatives: " + classifications[3]);
		System.out.println("Accuracy: "
				+ calculateAccuracy(classifications[0], classifications[1], classifications[2], classifications[3]));
		System.out.println("Precision: " + calculatePrecision(classifications[0], classifications[2]));
		System.out.println("Recall: " + calculateRecall(classifications[0], classifications[1]));
	}

	/**
	 * Extracts the test data from the input CSV file
	 * 
	 * @param input
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void parseTestCSV(String input) throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));

		String line = reader.readLine();
		while (((line = reader.readLine()) != null)) {
			String[] lineSplit = line.split(",");
			testExamples.add(lineSplit);
		}

		reader.close();
	}

	/**
	 * @param truePos
	 * @param falseNeg
	 * @param falsePos
	 * @param trueNeg
	 * @return accuracy
	 */
	private double calculateAccuracy(double truePos, double falseNeg, double falsePos, double trueNeg) {
		return (truePos + trueNeg) / (truePos + trueNeg + falsePos + falseNeg);
	}

	/**
	 * @param truePos
	 * @param falsePos
	 * @return precision
	 */
	private double calculatePrecision(double truePos, double falsePos) {
		return truePos / (truePos + falsePos);
	}

	/**
	 * @param truePos
	 * @param falseNeg
	 * @return recall
	 */
	private double calculateRecall(double truePos, double falseNeg) {
		return truePos / (truePos + falseNeg);
	}

}
