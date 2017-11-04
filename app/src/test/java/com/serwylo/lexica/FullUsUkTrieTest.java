package com.serwylo.lexica;

import com.serwylo.lexica.game.Board;
import com.serwylo.lexica.game.FourByFourBoard;

import net.healeys.trie.Solution;
import net.healeys.trie.StringTrie;
import net.healeys.trie.Trie;
import net.healeys.trie.WordFilter;

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FullUsUkTrieTest extends TrieTest {

	private static final Board BOARD = new FourByFourBoard(new Character[] {
			'R', 'Q', 'O', 'S',
			'W', 'N', 'O', 'A',
			'T', 'V', 'D', 'G',
			'N', 'P', 'U', 'I',
	});

	private static final LinkedHashMap<String, Solution> SOLUTIONS = new LinkedHashMap<>();

	static {
		//addSolution("QOD", xy(1, 0), xy(2, 1), xy(2, 2));
		addSolution("ONO", xy(2, 1), xy(1, 1), xy(2, 0));
		addSolution("SON", xy(3, 0), xy(2, 1), xy(1, 1));
		addSolution("SOON", xy(3, 0), xy(2, 1), xy(2, 0), xy(1, 1));
		addSolution("SOD", xy(3, 0), xy(2, 1), xy(2, 2));
		addSolution("SODA", xy(3, 0), xy(2, 1), xy(2, 2), xy(3, 1));
		addSolution("SAD", xy(3, 0), xy(3, 1), xy(2, 2));
		addSolution("SADI", xy(3, 0), xy(3, 1), xy(2, 2), xy(3, 3));
		addSolution("SAG", xy(3, 0), xy(3, 1), xy(3, 2));
		addSolution("SAGO", xy(3, 0), xy(3, 1), xy(3, 2), xy(2, 1));
		addSolution("NOD", xy(1, 1), xy(2, 1), xy(2, 2));
		addSolution("NODI", xy(1, 1), xy(2, 1), xy(2, 2), xy(3, 3));
		addSolution("NOG", xy(1, 1), xy(2, 1), xy(3, 2));
		addSolution("ADO", xy(3, 1), xy(2, 2), xy(2, 1));
		addSolution("AGO", xy(3, 1), xy(3, 2), xy(2, 1));
		addSolution("AGON", xy(3, 1), xy(3, 2), xy(2, 1), xy(1, 1));
		addSolution("DOS", xy(2, 2), xy(2, 1), xy(3, 0));
		addSolution("DON", xy(2, 2), xy(2, 1), xy(1, 1));
		addSolution("DOG", xy(2, 2), xy(2, 1), xy(3, 2));
		addSolution("DAG", xy(2, 2), xy(3, 1), xy(3, 2));
		addSolution("DAGO", xy(2, 2), xy(3, 1), xy(3, 2), xy(2, 1));
		addSolution("DAGOS", xy(2, 2), xy(3, 1), xy(3, 2), xy(2, 1), xy(3, 0));
		addSolution("DUG", xy(2, 2), xy(2, 3), xy(3, 2));
		addSolution("DIG", xy(2, 2), xy(3, 3), xy(3, 2));
		addSolution("GOO", xy(3, 2), xy(2, 1), xy(2, 0));
		addSolution("GOON", xy(3, 2), xy(2, 1), xy(2, 0), xy(1, 1));
		addSolution("GOS", xy(3, 2), xy(2, 1), xy(3, 0));
		addSolution("GOA", xy(3, 2), xy(2, 1), xy(3, 1));
		addSolution("GOAD", xy(3, 2), xy(2, 1), xy(3, 1), xy(2, 2));
		addSolution("GOV", xy(3, 2), xy(2, 1), xy(1, 2));
		addSolution("GOD", xy(3, 2), xy(2, 1), xy(2, 2));
		addSolution("GAS", xy(3, 2), xy(3, 1), xy(3, 0));
		addSolution("GAD", xy(3, 2), xy(3, 1), xy(2, 2));
		addSolution("GUV", xy(3, 2), xy(2, 3), xy(1, 2));
		addSolution("GUIDON", xy(3, 2), xy(2, 3), xy(3, 3), xy(2, 2), xy(2, 1), xy(1, 1));
		addSolution("GID", xy(3, 2), xy(3, 3), xy(2, 2));
		addSolution("PUD", xy(1, 3), xy(2, 3), xy(2, 2));
		addSolution("PUG", xy(1, 3), xy(2, 3), xy(3, 2));
		addSolution("UDO", xy(2, 3), xy(2, 2), xy(2, 1));
		addSolution("UPDO", xy(2, 3), xy(1, 3), xy(2, 2), xy(2, 1));

	}

	private static final String[] WORDS = new String[] {
			/*"QOD",*/ "ONO", "SON", "SOON", "SOD", "SODA", "SAD", "SADI", "SAG", "SAGO", "NOD", "NODI",
			"NOG", "ADO", "AGO", "AGON", "DOS", "DON", "DOG", "DAG", "DAGO", "DAGOS", "DUG", "DIG",
			"GOO", "GOON", "GOS", "GOA", "GOAD", "GOV", "GOD", "GAS", "GAD", "GUV", "GUIDON", "GID",
			"PUD", "PUG", "UDO", "UPDO",
	};

	private static int xy(int x, int y) {
		return TransitionMapTest.xy(x, y);
	}

	private static void addSolution(String word, Integer ...positions) {
		SOLUTIONS.put(word, new Solution.Default(word, positions));
	}

	@Test
	public void testLoadingCompressedTries() throws IOException {
		InputStream stream = FullUsUkTrieTest.class.getClassLoader().getResourceAsStream("en_us.bin");
		Trie trie = new StringTrie.Deserializer().deserialize(stream, BOARD);
		assertTrieCorrect(trie);
	}

	private static void assertTrieCorrect(Trie trie) {
		Map<String, Solution> solutions = trie.solver(BOARD, new WordFilter.MinLength(3));
		List<String> expectedWords = new ArrayList<>();
		Collections.addAll(expectedWords, WORDS);

		List<String> actualWords = new ArrayList<>();
		for (String w : solutions.keySet()) {
			actualWords.add(w);
		}

		Collections.sort(expectedWords);
		Collections.sort(actualWords);

		assertEquals(expectedWords, actualWords);

		for (Map.Entry<String, Solution> actualEntry : solutions.entrySet()) {
			Solution actualSolution = actualEntry.getValue();
			assertEquals(actualEntry.getKey(), actualSolution.getWord());

			boolean found = false;
			for (Map.Entry<String, Solution> expectedEntry : SOLUTIONS.entrySet()) {
				if (expectedEntry.getKey().equals(actualEntry.getKey())) {
					found = true;

					Integer[] expectedPositions = expectedEntry.getValue().getPositions();
					Integer[] actualPositions = actualEntry.getValue().getPositions();

					assertArrayEquals("Comparing solutions for: " + expectedEntry.getKey(), expectedPositions, actualPositions);
				}
			}

			assertTrue(found);
		}
	}

	// Used to test performance optimizations. Remove @Ignore to use it.
	@Test
	@Ignore
	public void testSolverPerformance() throws IOException {
		InputStream stream = FullUsUkTrieTest.class.getClassLoader().getResourceAsStream("en_us.bin");
		Trie trie = new StringTrie.Deserializer().deserialize(stream, BOARD);
		assertEquals(40, trie.solver(BOARD, new WordFilter.MinLength(3)).size());
		long startTime = System.currentTimeMillis();
		trie.solver(BOARD, new WordFilter.MinLength(3));
		long totalTime = (System.currentTimeMillis() - startTime) ;
		fail("Took " + totalTime + "ms");
	}

	@Test
	public void testStringTrieUsDictionary() {
		testUsDictionary(new StringTrie());
	}

	private void testUsDictionary(Trie trie) {
		String[] words = readDictionary("en_us.txt");
		assertEquals(74007, words.length);

		addWords(trie, words);

		assertTrieMatches("After adding entire US dictionary to a new Trie", trie, words);
	}

	@Test
	public void testStringTrieUkDictionary() {
		testUkDictionary(new StringTrie());
	}

	private void testUkDictionary(Trie trie) {
		String[] words = readDictionary("en_uk.txt");
		assertEquals(73596, words.length);

		addWords(trie, words);

		assertTrieMatches("After adding entire UK dictionary to a new Trie", trie, words);
	}

	static String[] readDictionary(String fileName) {
		try {
			List<String> words = new ArrayList<>(80000);
			InputStream stream = FullUsUkTrieTest.class.getClassLoader().getResourceAsStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line = reader.readLine();
			while (line != null) {
				words.add(line);
				line = reader.readLine();
			}
			String[] wordsArray = new String[words.size()];
			words.toArray(wordsArray);
			return wordsArray;
		} catch (IOException e) {
			fail();
			throw new RuntimeException(e);
		}
	}

}
