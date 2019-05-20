package io.spring.sample.trie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.http.server.PathContainer;
import org.springframework.util.AntPathMatcher;

public class PatternsTrie<T> {

	private final PatternsTrieNode<T> root;

	private final int maxDepth;

	private final AntPathMatcher matcher = new AntPathMatcher();

	public PatternsTrie(int maxDepth) {
		this.maxDepth = maxDepth;
		this.root = new PatternsTrieNode<T>();
	}

	public void insert(PathContainer path, T match) {
		findNode(path).addMatch(match);
	}

	public void remove(PathContainer path, T info) {
		PatternsTrieNode<T> node = findNode(path);
		node.removeMatch(info);
		if (node.isEmpty()) {
			node.getParent().remove(node);
		}
	}

	private PatternsTrieNode<T> findNode(PathContainer path) {
		int depth = 0;
		PatternsTrieNode<T> current = this.root;
		Iterator<PathContainer.Element> elementIterator = path.elements().iterator();
		while(elementIterator.hasNext() && depth <= this.maxDepth) {
			PathContainer.Element next = elementIterator.next();
			if(next instanceof PathContainer.PathSegment) {
				PathContainer.PathSegment segment = (PathContainer.PathSegment) next;
				if (matcher.isPattern(segment.valueToMatch())) {
					break;
				}
				final PatternsTrieNode<T> parent = current;
				current = current.getChildren()
						.computeIfAbsent(segment, s -> new PatternsTrieNode<T>(parent));
				depth++;
			}
		}
		return current;
	}

	public List<T> selectMatches(PathContainer pathContainer) {
		PatternsTrieNode<T> current = this.root;
		List<T> matches = new ArrayList<>(current.getMatches());
		Iterator<PathContainer.Element> elements = pathContainer.elements().iterator();
		while (current != null && elements.hasNext()) {
			PathContainer.Element element = elements.next();
			if (element instanceof PathContainer.PathSegment) {
				PathContainer.PathSegment segment = (PathContainer.PathSegment) element;
				current = current.getChildren().get(segment);
				if (current != null) {
					matches.addAll(current.getMatches());
				}
			}
		}
		return matches;
	}

}
