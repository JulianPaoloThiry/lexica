package com.serwylo.lexica;

import net.healeys.trie.Trie;

import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class TrieTest {

	private static final String[] NOT_WORDS = new String[] {
			"NOTAWORD",
			"DEFINITELYNOTAWORD",
			"WELLTHISISEMBARRASSING",
			"BLZH",
			"SNZH"
	};

	static void assertTrieMatches(String message, Trie trie, String[] bothDialects) {
		if (bothDialects != null) {
			for (String word : bothDialects) {
				String log = word + " [BOTH]";
				Assert.assertTrue(log + " should be a word", trie.isWord(word));
			}
		}

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
