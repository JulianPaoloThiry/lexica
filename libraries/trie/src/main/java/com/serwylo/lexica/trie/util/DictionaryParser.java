package com.serwylo.lexica.trie.util;

import net.healeys.trie.StringTrie;
import net.healeys.trie.Trie;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryParser {

	private static final List<File> inputFiles= new ArrayList<>();
	private static final List<String> outputFolders= new ArrayList<>();

	private Trie trie = new StringTrie();
	private Map<Character, Map<Character, Integer>> counts =  new HashMap<>();
	private Map<Character, Integer> wordLengths = new HashMap<>();
	private Map<Character, Integer> letterCounts = new HashMap<>();
	private Map<Character, Integer> letterPoints = new HashMap<>();

	private void run(File inputFile) throws IOException {
		counts.put(' ', new HashMap<Character, Integer>());

		readFileIntoTrie(new FileInputStream(inputFile), trie);

		for (String outputFolder : outputFolders) {
			writeTrie(outputFolder, inputFile);
			writeStats(outputFolder, inputFile);
		}
	}

	private void readFileIntoTrie(InputStream dictFile, Trie trie) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(dictFile, StandardCharsets.UTF_8));
		String line;
		while ((line = br.readLine()) != null) {
			trie.addWord(line);
			count(line);
		}
		scaleCounts();
	}

	private void writeTrie(String outputFolder, File inputFile) throws IOException {
		String name = outputFolder + File.separator + inputFile.getName().replace(".txt", ".bin");
		try (FileOutputStream of = new FileOutputStream(name, false)) {
			trie.write(new DataOutputStream(of));
		}
	}

	private void writeStats(String outputFolder, File inputFile) throws IOException {
		String name = outputFolder + File.separator + inputFile.getName().replace(".txt", "_stats.txt");
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(name), StandardCharsets.UTF_8)) {
			writeMap(osw, '@', letterPoints);
			writeMap(osw, '#', wordLengths);
			for (Map.Entry<Character, Map<Character, Integer>> counter : counts.entrySet()) {
				if (!counter.getKey().equals('#'))
					writeMap(osw, counter.getKey(), counter.getValue());
			}
		}
	}

	private void writeMap(OutputStreamWriter fw, Character key, Map<Character, Integer> map) throws IOException {
		fw.append(key.toString()).append("\t");
		for (Map.Entry<Character, Integer> entry : map.entrySet()) {
			fw.append(entry.getKey().toString()).append(":")
					.append(Integer.toString(entry.getValue()))
					.append("\t");
		}
		fw.append("\n");

	}

	/**
	 * Build a map of maps that represents a sparse table. The rows and columns both represent the
	 * letters found in the words. The value in a cell represents the number of times the row letter
	 * is followed by the column letter. The ' ' row, however, represents the number of times the
	 * column letter starts a word.
	 *
	 * Two additional "rows" are maintained: wordLengths counts words by length, and letterCounts
	 * counts the total number of occurrences of each letter.
	 *
	 * @param word word
	 */
	private void count(String word) {
		if (word.length() < 3) return;
		incCounter(wordLengths, (char)(word.length()+'>'));

		char prev = word.charAt(0);
		incCounter(getCounter(' '), prev);
		incCounter(letterCounts, prev);

		Map<Character, Integer> counter = getCounter(prev);
		for (int i = 1; i < word.length(); i++) {
			char curr = word.charAt(i);
			incCounter(counter, curr);
			incCounter(letterCounts, curr);
			counter = getCounter(curr);
		}
	}


	private void scaleCounts() {
		//Find min and max letter counts. This establishes the current (old) scale.
		int oldMin = Integer.MAX_VALUE;
		int oldMax = 0;
		for (Integer sum : letterCounts.values()) {
			if (sum < oldMin) oldMin = sum;
			if (sum > oldMax) oldMax = sum;
		}

		double oldRatio = oldMax / (double)oldMin;
		double oldRange = oldMax - (double)oldMin;

		//We want to compress the scale such that the most likely letter will occur X times more
		//often than the least likely letter, where X is the number of letters in the alphabet.
		//This simply avoids having the least likely letter show up too rarely. However, if the
		//existing (old) scale is already under this threshold, we won't change it. We still do
		//most of the work, though, because we're going to need these percentiles in order to
		//calculate letter points.

		//Establish the new range.
		int numSymbols = counts.size() - 1;
		double newRatio = 1d / numSymbols;
		double newMin = oldMax * newRatio;
		double newRange = oldMax - newMin;

		//For each old letter count, we will calculate the percentile in the old range, then find
		//the ratio required to scale all letter frequencies to values that will hit the same
		//percentile in the new range. If needed, we'll then apply that ratio to all letter frequencies.
		//We'll also use the percentile to calculate letter points and store them separately.
		for (Map.Entry<Character, Integer> entry: letterCounts.entrySet()) {
			Character key = entry.getKey();
			int oldSum = entry.getValue();
			double percentile = (oldSum - oldMin) / oldRange;
			letterPoints.put(key, points(percentile));
			if (oldRatio > newRatio) {
				int newSum = (int) Math.round(percentile * newRange + newMin);
				double ratio = newSum / (double)oldSum;
				for (Map<Character, Integer> row : counts.values()) {
					Integer letterCount = row.get(key);
					if (letterCount != null) {
						row.put(key, (int) Math.round(letterCount * ratio));
					}
				}
			}
		}

		//Add the word lengths into counts so that the following normalization applies to it as well.
		counts.put('#', wordLengths);

		//Finally, we'll normalize the frequency values so that each number represents the
		//probability of choosing the column letter next, given the row number, out of 1000
		//(approximately - rounding means the actual sum is likely slightly higher).
		for (Map<Character, Integer> row : counts.values()) {
			//We need to recalculate the sum due to rounding differences.
			int sum = 0;
			for (Integer value : row.values()) {
				sum += value;
			}

			for (Map.Entry<Character, Integer> entry : row.entrySet()) {
				//We use ceiling here so that zero is never a possibility. True zero probability
				//is represented by the lack of an entry in the map.
				entry.setValue((int) Math.ceil(entry.getValue() * 1000d / sum));
			}
		}

	}

	//This doesn't feel right. It works well for English, but still seems contrived.
	private int points(double ratio) {
		if (ratio > 1/2d) return 1;
		if (ratio > 1/4d) return 2;
		if (ratio > 1/8d) return 3;
		if (ratio > 1/16d) return 4;
		if (ratio > 1/32d) return 5;
		if (ratio > 1/64d) return 6;
		if (ratio > 1/128d) return 7;
		if (ratio > 1/256d) return 8;
		if (ratio > 1/512d) return 9;
		return 10;
	}

	private Map<Character, Integer> getCounter(char c) {
		Map<Character, Integer> counter = counts.get(c);
		if (counter == null) {
			counter = new HashMap<>();
			counts.put(c, counter);
		}
		return counter;
	}

	private void incCounter(Map<Character, Integer> counter, char c) {
		Integer i = counter.get(c);
		if (i == null) {
			counter.put(c, 1);
		} else {
			counter.put(c, i + 1);
		}
	}

	public static void main(String[] args) throws IOException {
		boolean in = true;
		for (String arg : args) {
			if ("-".equals(arg)) {
				in = false;
			} else if (in) {
				inputFiles.add(new File(arg));
			} else {
				outputFolders.add(arg);
			}
		}

		if (inputFiles.isEmpty() || outputFolders.isEmpty()) {
			printUsage();
			return;
		}


		for (File f : inputFiles) {
			if (!f.exists()) {
				printFileNotFound(f);
				return;
			}
		}

		for (File f : inputFiles) {
			new DictionaryParser().run(f);
		}
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("    java -jar trie-builder.jar dicts - binfolders");
		System.out.println("        dicts ...        Input text file(s), one word per line.");
		System.out.println("        binfolders ...   Output folder(s)");
	}

	private static void printFileNotFound(File file) {
		System.out.println("Input file " + file + " does not exist.");
		printUsage();
	}


}
