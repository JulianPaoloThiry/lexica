package net.healeys.trie;

import java.io.IOException;
import java.io.OutputStream;

public interface TrieNode {

	void writeNode(OutputStream out) throws IOException;

	TrieNode addSuffix(String word, int currentPosition);

	boolean isWord();

	boolean isTail();

	boolean isWord(String word, int currentPosition);

}
