package com.serwylo.lexica.trie.tests;

import net.healeys.trie.StringTrie;
import net.healeys.trie.Trie;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FullTrieTest extends TrieTest {

	@Test
	public void testUsDictionary() {
		String[] words = readDictionary("en_us.txt");
		Assert.assertEquals(74007, words.length);

		Trie trie = new StringTrie();
		addWords(trie, words);

		assertTrieMatches("After adding entire US dictionary to a new Trie", trie, words);
	}

	@Test
	public void testEsDictionary() {
		String[] words = readDictionary("es.txt");
		Assert.assertEquals(78984, words.length);

		Trie trie = new StringTrie();
		addWords(trie, words);

		assertTrieMatches("After adding entire ES dictionary to a new Trie", trie, words);
	}

	public static String[] readDictionary(String fileName) {
		try {
			List<String> words = new ArrayList<>(80000);
			InputStream stream = FullTrieTest.class.getClassLoader().getResourceAsStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			String line = reader.readLine();
			while (line != null) {
				words.add(line);
				line = reader.readLine();
			}
			String[] wordsArray = new String[words.size()];
			words.toArray(wordsArray);
			return wordsArray;
		} catch (IOException e) {
			Assert.fail();
			throw new RuntimeException(e);
		}
	}

}
