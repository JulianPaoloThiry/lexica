package com.serwylo.lexica.trie.tests;

import net.healeys.trie.Solution;
import net.healeys.trie.Trie;
import net.healeys.trie.WordFilter;

import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class TrieTest {

	private static final String[] NOT_WORDS = new String[] {
			"NOTAWORD",
			"DEFINITELYNOTAWORD",
			"WELLTHISISEMBARRASSING",
			"BLZH",
			"SNZH"
	};

	private static void onlyContains(Trie trie, Set<String> expectedWords) {
		Map<String, Solution> solutions = trie.solver(new CanTransitionMap(), new WordFilter() {
			@Override
			public boolean isWord(String word) {
				return true;
			}
		});

		List<String> expected = new ArrayList<>(expectedWords);
		List<String> actual = new ArrayList<>(solutions.keySet());

		Collections.sort(expected);
		Collections.sort(actual);

//		for (int i = 0; i < expected.size(); i++) {
//			assertEquals(expected.get(i), actual.get(i));
//		}
		assertArrayEquals("Words don't match", expected.toArray(), actual.toArray());
	}

	static void assertTrieMatches(String message, Trie trie, String[] bothDialects) {
		Set<String> allWords = new HashSet<>();
		if (bothDialects != null) {
			for (String word : bothDialects) {
				String log = word + " [BOTH]";
				allWords.add(word);
				assertTrue(log + " should be a word", trie.isWord(word));
			}
		}

		onlyContains(trie, allWords);

		for (String notAWord : NOT_WORDS) {
			String log = notAWord + " should not be a word";
			Assert.assertFalse(log, trie.isWord(notAWord));
		}
	}

	public static void addWords(Trie trie, String[] words) {
		for (String word : words) {
			trie.addWord(word);
		}
	}

	public static byte[] serialize(Trie trie) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			trie.write(outputStream);
			return outputStream.toByteArray();
		} catch (IOException e) {
			Assert.fail();
			return null;
		}
	}

}
