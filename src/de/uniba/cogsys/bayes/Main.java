package de.uniba.cogsys.bayes;

import java.io.IOException;


public class Main {

	public static void main(String[] args) {

		if (args.length != 2 || !args[0].endsWith(".csv") || !args[1].endsWith(".csv")) {
			System.out.println("Invalid input, need a training and a test csv file as system argument");
			System.exit(0);
		}

		NaiveBayesClassifier classifier = new NaiveBayesClassifier();
		try {
			classifier.training(args[0], "class", "p", "e");
			classifier.testData(args[1]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
