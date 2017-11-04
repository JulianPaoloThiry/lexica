/*
 *  Copyright (C) 2008-2009 Rev. Johnny Healey <rev.null@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.serwylo.lexica.game;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LetterChainGenerator {
	@SuppressWarnings("unused")
	private static final String TAG = "LetterChainGenerator";

	private static final int[] place16 = new int[]{
			5, 4, 0, 1, 2, 3, 7, 6, 10, 11, 15, 14, 13, 12, 8, 9
	};
	private static final int[] place25 = new int[]{
			12, 11, 10, 5, 0, 1, 6, 7, 2, 3, 4, 9, 8, 13, 14, 19, 24, 23, 18, 17, 22, 21, 20, 15, 16
	};
	private static final int[] place36 = new int[]{
			20,19,18,24,30,31,25,26,32,33,27,28,34,35,29,23,22,21,15,16,17,11,5,4,10,9,3,2,8,7,1,0,6,12,13,14
	};

	private final Map<Character, Map<Character, Integer>> probabilities = new HashMap<>();
	private final Map<Character, Integer> letterPoints;

	public LetterChainGenerator(InputStream letter_stream) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				letter_stream, StandardCharsets.UTF_8))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String letters[] = line.split("\t");
				Character key = letters[0].charAt(0);
				Map<Character, Integer> map = getMap(key);
				map.put('+', 0);

				int sum = 0;
				for (int y = 1; y < letters.length; y++) {
					if (!letters[y].isEmpty()) {
						String[] chunks = letters[y].split(":");
						int count = Integer.valueOf(chunks[1]);
						map.put(chunks[0].charAt(0), count);
						map.put('+', map.get('+') + count);
					}
				}
			}
		} catch (Exception e) {
			// Log.e(TAG,"READING INPUT",e);
			// Checked exceptions considered harmful.
		}
		letterPoints = probabilities.remove('@');
	}

	private Map<Character, Integer> getMap(char key) {
		Map<Character, Integer> map = probabilities.get(key);
		if (map == null) {
			map = new HashMap<>();
			probabilities.put(key, map);
		}
		return map;
	}


	public FiveByFiveBoard generateFiveByFiveBoard() {
		return new FiveByFiveBoard(generateBoard(25));
	}

	public FourByFourBoard generateFourByFourBoard() {
		return new FourByFourBoard(generateBoard(16));
	}

	public SixBySixBoard generateSixBySixBoard() {
		return new SixBySixBoard(generateBoard(36));
	}

	public Character[] generateBoard(int size) {
		int wordLen = 0;
		Random rng = new Random();

		Character board[] = new Character[size];

		Character c = ' ';
		Character prev = ' ';
		//Create words of probable lengths. This enforced break ensures that, if a dictionary
		//happens to have letters that are only found at the beginning of words, there is at
		//least some chance of having more than one of them per board.
		int length = getRandomWordLength();
		for (int i = 0; i < size; i++) {
			wordLen++;
			int rand = rng.nextInt(probabilities.get(c).get('+'));
			int sum = 0;
			for (Map.Entry<Character, Integer> entry : probabilities.get(c).entrySet()) {
				if (!entry.getKey().equals('+')) {
					sum += entry.getValue();
					if (rand <= sum) {
						c = entry.getKey();
						if (prev != ' ') {
							//Note that we reduce the previous letter rather than the current
							//so that we don't ruin the changes of commonly doubled letters.
							reduce(prev);
						}
						prev = c;
						board[i] = c;
						break;
					}
				}
			}

			if (wordLen == length) {
				wordLen = 0;
				c = ' ';
				length = getRandomWordLength();
			}

		}

		int[] placement = size == 16 ? place16 : (size == 25 ? place25 : place36);

		// place the letters in a transitionable pattern
		for (int from = 0; from < size; from++) {
			int to = placement[from];
			Character tmp = board[to];
			board[to] = board[from];
			board[from] = tmp;
		}

		return board;
	}

	private int getRandomWordLength() {
		Map<Character, Integer> lengths = probabilities.get('#');
		int lengthTotal = lengths.get('+');

		Random rng = new Random();
		int rand = rng.nextInt(lengthTotal);
		int sum = 0;
		for (Map.Entry<Character, Integer> entry : lengths.entrySet()) {
			sum += entry.getValue();
			if (rand <= sum) {
				return entry.getKey() - '>';
			}
		}
		return 0;
	}

	/**
	 * Once a letter has been placed on the board, reduce the overall probability of drawing the
	 * same letter by 50%.
	 *
	 * @param c letter
	 */
	private void reduce(Character c) {
		for (Map<Character, Integer> map : probabilities.values()) {
			Integer val = map.get(c);
			if (val != null) {
				map.put(c, val - (val / 2));
				map.put('+', map.get('+') - (val / 2));
			}
		}
	}

	public Map<Character, Integer> getLetterPoints() {
		return letterPoints;
	}
}
