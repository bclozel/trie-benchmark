package io.spring.sample.trie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.server.PathContainer;

class PatternsTrieNode<T> {

	private final PatternsTrieNode parent;

	private final HashMap<PathContainer.PathSegment, PatternsTrieNode<T>> children = new HashMap<>(8);

	private List<T> matches = new ArrayList<>();

	PatternsTrieNode() {
		this(null);
	}

	PatternsTrieNode(PatternsTrieNode parent) {
		this.parent = parent;
	}

	PatternsTrieNode getParent() {
		return parent;
	}

	HashMap<PathContainer.PathSegment, PatternsTrieNode<T>> getChildren() {
		return this.children;
	}

	boolean remove(PatternsTrieNode node) {
		return this.children.values().remove(node);
	}

	boolean isEmpty() {
		return this.children.isEmpty() && this.matches.isEmpty();
	}

	List<T> getMatches() {
		return this.matches;
	}

	void addMatch(T match) {
		this.matches.add(match);
	}

	boolean removeMatch(T match) {
		return this.matches.remove(match);
	}
}
