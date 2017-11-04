package com.serwylo.lexica;

import com.serwylo.lexica.game.Board;
import com.serwylo.lexica.game.FourByFourBoard;

import net.healeys.trie.Solution;
import net.healeys.trie.StringTrie;
import net.healeys.trie.Trie;
import net.healeys.trie.WordFilter;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransitionMapTest {

    private static final Board BOARD = new FourByFourBoard(new Character[] {
            'B', 'E', 'X', 'X',
            'D', 'A', 'N', 'X',
            'X', 'R', 'K', 'X',
            'X', 'X', 'X', 'X',
    });

    // Only a subset of all possible solutions.
    private static final LinkedHashMap<String, Solution> SOLUTIONS = new LinkedHashMap<>();

    static {
        // Note: Don't include "A", "AN", or "BE" because they are too short.
        SOLUTIONS.put("BED",   new Solution.Default("BED",   new Integer[] { xy(0, 0), xy(1, 0), xy(0, 1) }));
        SOLUTIONS.put("BAD",   new Solution.Default("BAD",   new Integer[] { xy(0, 0), xy(1, 1), xy(0, 1) }));
        SOLUTIONS.put("BAN",   new Solution.Default("BAN",   new Integer[] { xy(0, 0), xy(1, 1), xy(2, 1) }));
        SOLUTIONS.put("RAN",   new Solution.Default("RAN",   new Integer[] { xy(1, 2), xy(1, 1), xy(2, 1) }));
        SOLUTIONS.put("BEAN",  new Solution.Default("BEAN",  new Integer[] { xy(0, 0), xy(1, 0), xy(1, 1), xy(2, 1) }));
        SOLUTIONS.put("BANE",  new Solution.Default("BANE",  new Integer[] { xy(0, 0), xy(1, 1), xy(2, 1), xy(1, 0) }));
        SOLUTIONS.put("BARN",  new Solution.Default("BARN",  new Integer[] { xy(0, 0), xy(1, 1), xy(1, 2), xy(2, 1) }));
        SOLUTIONS.put("DARN",  new Solution.Default("DARN",  new Integer[] { xy(0, 1), xy(1, 1), xy(1, 2), xy(2, 1) }));
        SOLUTIONS.put("BEARD", new Solution.Default("BEARD", new Integer[] { xy(0, 0), xy(1, 0), xy(1, 1), xy(1, 2), xy(0, 1) }));
        SOLUTIONS.put("EAR",   new Solution.Default("EAR",   new Integer[] { xy(1, 0), xy(1, 1), xy(1, 2) }));
        SOLUTIONS.put("EARN",  new Solution.Default("EARN",  new Integer[] { xy(1, 0), xy(1, 1), xy(1, 2), xy(2, 1) }));
        SOLUTIONS.put("BARD",  new Solution.Default("BARD",  new Integer[] { xy(0, 0), xy(1, 1), xy(1, 2), xy(0, 1) }));
    }

    static int xy(int x, int y) {
        return x + BOARD.getWidth() * y;
    }

    @Test
    public void stringTransitionTest() throws IOException {
        byte[] serialized = serializedUsTrie(new StringTrie());
        Trie trie = new StringTrie.Deserializer().deserialize(new ByteArrayInputStream(serialized), BOARD);
        Map<String, Solution> actualSolutions = trie.solver(BOARD, new WordFilter.MinLength(3));
        assertSolutions(SOLUTIONS, actualSolutions);
    }

    private static void assertSolutions(Map<String, Solution> expectedSolutions, Map<String, Solution> actualSolutions) {
        Set<String> expectedWords = expectedSolutions.keySet();
        Set<String> actualWords = actualSolutions.keySet();
        assertTrue(actualWords.containsAll(expectedWords));

        for (Map.Entry<String, Solution> expected : expectedSolutions.entrySet()) {
            Solution expectedSolution = expected.getValue();
            String expectedWord = expected.getKey();
            boolean found = false;
            for (Map.Entry<String, Solution> actual : actualSolutions.entrySet()) {
                Solution actualSolution = actual.getValue();
                String actualWord = actual.getKey();

                if (expectedWord.equals(actualWord)) {
                    found = true;
                    assertSolutionEquals(expectedSolution, actualSolution);
                }
            }

            assertTrue(found);
        }
    }

    private static void assertSolutionEquals(Solution expectedSolution, Solution actualSolution) {
        assertEquals(expectedSolution.getWord(), actualSolution.getWord());
        assertArrayEquals("Word: " + expectedSolution.getWord(), expectedSolution.getPositions(), actualSolution.getPositions());
    }

    private static byte[] serializedUsTrie(Trie trie) {
        TrieTest.addWords(trie, FullUsUkTrieTest.readDictionary("en_us.txt"));
        return TrieTest.serialize(trie);
    }

}
