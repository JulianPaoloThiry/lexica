package net.healeys.trie;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StringTrie implements Trie {

	private final Node rootNode;

	public StringTrie() {
		rootNode = new Node();
	}

	/**
	 * Decides whether you can transition from one cell to another, purely based on whether both the
	 * source and destination cell are present on the board.
	 *
	 * It doesn't have enough information to figure out which words can and can't be done correctly.
	 * However it does have enough information to exclude large portions of a dictionary-sized
	 * trie very quickly, instead of spending time reading and parsing it.
     * Using this approximately halves the loading time in my basic tests.
	 */
	private static class CheapTransitionMap {

		private Map<Character, Set<Character>> transitions = new HashMap<>();

		CheapTransitionMap(TransitionMap transitionMap) {
			for (int fromPos = 0; fromPos < transitionMap.getSize(); fromPos ++) {

				int fromX = fromPos % transitionMap.getWidth();
				int fromY = fromPos / transitionMap.getWidth();

				Set <Character> transitionTo = new HashSet<>();
				for (int j = 0; j < transitionMap.getSize(); j ++) {
					int toX = j % transitionMap.getWidth();
					int toY = j / transitionMap.getWidth();
					if (transitionMap.canTransition(fromX, fromY, toX, toY)) {
						transitionTo.add(transitionMap.valueAt(j));
					}
				}

				Character from = transitionMap.valueAt(fromPos);

				if (!transitions.containsKey(from)) {
					transitions.put(from, new HashSet<Character>());
				}

				transitions.get(from).addAll(transitionTo);

			}
		}

		boolean contains(Character from) {
			return transitions.containsKey(from);
		}

		boolean canTransition(Character from, Character to) {
			Set<Character> transitionTo = transitions.get(from);
			return transitionTo != null && transitionTo.contains(to);
		}
	}

	private StringTrie(InputStream in, TransitionMap transitionMap) throws IOException {
		Set<Character> availableStrings = new HashSet<>(transitionMap.getSize());
		for (int i = 0; i < transitionMap.getSize(); i ++) {
			availableStrings.add(transitionMap.valueAt(i));
		}
		rootNode = new Node(new DataInputStream(new BufferedInputStream(in)), new CheapTransitionMap(transitionMap), availableStrings, false, null, 0);
	}

	@Override
	public void addWord(String w) {
		rootNode.addSuffix(w, 0);
	}

	@Override
	public boolean isWord(String word) {
		return rootNode.isWord(word, 0);
	}

	@Override
	public void write(OutputStream out) throws IOException {
		rootNode.writeNode(out);
	}

	private void recursiveSolver(
			TransitionMap transitions,
			WordFilter wordFilter,
			StringTrie.Node node,
			int pos,
			Set<Integer> usedPositions,
			StringBuilder prefix,
			Map<String, Solution> solutions,
			List<Integer> solution) {

		if (node.isWord()) {
			String w = new String(prefix);
			if(wordFilter == null || wordFilter.isWord(w)) {
				Integer[] solutionArray = new Integer[solution.size()];
				solution.toArray(solutionArray);
				solutions.put(w, new Solution.Default(w, solutionArray));
			}
		}

		if (node.isTail()) {
			return;
		}

		if (!transitions.canRevisit()) {
			usedPositions.add(pos);
		}

		int fromX = pos % transitions.getWidth();
		int fromY = pos / transitions.getWidth();

		for (int toX = 0; toX < transitions.getWidth(); toX ++) {
			for	(int toY = 0; toY < transitions.getWidth(); toY ++) {
				if (!transitions.canTransition(fromX, fromY, toX, toY)) {
					continue;
				}

				int toPosition = toX + transitions.getWidth() * toY;
				if (usedPositions.contains(toPosition)) {
					continue;
				}

				Character valueAt = transitions.valueAt(toPosition);
				StringTrie.Node nextNode = node.maybeChildAt(valueAt);
				if (nextNode == null) {
					continue;
				}

				prefix.append(valueAt);

				solution.add(toPosition);
				recursiveSolver(transitions, wordFilter, nextNode, toPosition, usedPositions, prefix, solutions, solution);
				solution.remove(solution.size() - 1);

				prefix.delete(prefix.length() - 1, prefix.length());
			}
		}

		usedPositions.remove(pos);
	}

	@Override
	public Map<String, Solution> solver(TransitionMap transitions, WordFilter filter) {

		//long startTime = System.currentTimeMillis();
		Map<String, Solution> solutions = new TreeMap<>();
		StringBuilder prefix = new StringBuilder(transitions.getSize() + 1);

		List<Integer> positions = new ArrayList<>(transitions.getSize());
		for(int i=0; i < transitions.getSize(); i ++) {
			Character value = transitions.valueAt(i);
			StringTrie.Node nextNode = rootNode.maybeChildAt(value);
			if (nextNode == null) {
				continue;
			}

			prefix.append(value);
			positions.add(i);

			recursiveSolver(transitions, filter, nextNode, i, new HashSet<Integer>(), prefix, solutions, positions);

			positions.remove(positions.size() - 1);
			prefix.delete(prefix.length() - 1, prefix.length());
		}

		//long totalTime = System.currentTimeMillis() - startTime;

		return solutions;
	}

	private static class Node implements TrieNode {

		private final Map<Character, Node> children = new HashMap<>();

		private boolean isWord;

		private Node() {

		}

		private Node(DataInputStream input, CheapTransitionMap transitionMap, Set<Character> availableStrings, boolean shouldSkip, Character lastChar, int depth) throws IOException {

			int nodeSizeInBytes = input.readInt();

			if (shouldSkip) {
				input.skipBytes(nodeSizeInBytes);
				return;
			}

			int numBytes = input.readByte();

			if (numBytes < 0) {
				isWord = true;
				numBytes = -1 - numBytes;
			}

			if (numBytes > 0) {
				byte[] bytes = new byte[numBytes];
				input.read(bytes);
				String childKeysAsString = new String(bytes, StandardCharsets.UTF_8);
				Character[] childStrings = new Character[childKeysAsString.length()];
				for (int i = 0; i < childKeysAsString.length(); i++) {
					Character chr = childKeysAsString.charAt(i);
					if (depth == 0 && transitionMap.contains(chr) || depth > 0 && transitionMap.canTransition(lastChar, chr)) {
						childStrings[i] = chr;
					}
				}

				for (int i = 0; i < childStrings.length; i++) {
					// Need to read the node regardless of whether we end up keeping it. This is to
					// ensure that we traverse the InputStream in the right order.
					boolean shouldSkipChild = childStrings[i] == null;
					Node childNode = new Node(input, transitionMap, availableStrings, shouldSkipChild, childStrings[i], depth + 1);
					if (!shouldSkipChild) {
						children.put(childStrings[i], childNode);
					}
				}
			}
		}

		@Override
		public void writeNode(OutputStream output) throws IOException {
			StringBuilder childKeys = new StringBuilder();
			for (Character c : children.keySet())
				childKeys.append(c);
			byte[] bytes = childKeys.toString().getBytes(StandardCharsets.UTF_8);

			int b = bytes.length;
			if (b > Byte.MAX_VALUE)
				throw new IOException("Too many distinct characters in dictionary: bytes.length "
						+ b + " > " + Byte.MAX_VALUE);

			if (isWord)
				b = (byte)(-1 - b);

			ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
			DataOutputStream tempOutputData = new DataOutputStream(tempOutput);

			tempOutputData.writeByte(b);
			tempOutputData.write(bytes, 0, bytes.length);

			for (Node child : children.values()) {
				child.writeNode(tempOutputData);
			}

			DataOutputStream outputData = new DataOutputStream(output);
			outputData.writeInt(tempOutput.size());
			outputData.write(tempOutput.toByteArray());
		}

		@Override
		public TrieNode addSuffix(String word, int currentPosition) {
			Node child = ensureChildAt(word, currentPosition);

			if (currentPosition == word.length() - 1) {
				child.isWord = true;
				return child;
			} else {
				return child.addSuffix(word, currentPosition + 1);
			}
		}

		private Node maybeChildAt(String word, int position) {
			return children.get(word.charAt(position));
		}

		private Node maybeChildAt(Character childChar) {
			return children.get(childChar);
		}

		private Node ensureChildAt(String word, int position) {
			char character = word.charAt(position);
			Node existingNode = maybeChildAt(word, position);
			if (existingNode == null) {
				Node node = new Node();
				children.put(character, node);
				return node;
			} else {
				return existingNode;
			}
		}

		@Override
		public boolean isWord() {
			return isWord;
		}

		@Override
		public boolean isTail() {
			return children.size() == 0;
		}

		@Override
		public boolean isWord(String word, int currentPosition) {
			if (currentPosition == word.length()) {
				return isWord;
			}

			Node childNode = maybeChildAt(word, currentPosition);
			return childNode != null && childNode.isWord(word, currentPosition + 1);
		}

	}

	public static class Deserializer implements net.healeys.trie.Deserializer<StringTrie> {
		@Override
		public StringTrie deserialize(InputStream stream, TransitionMap transitionMap) throws IOException {
			return new StringTrie(stream, transitionMap);
		}
	}

}
