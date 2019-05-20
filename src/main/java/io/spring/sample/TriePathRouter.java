package io.spring.sample;

import java.util.ArrayList;
import java.util.List;

import io.spring.sample.trie.PatternsTrie;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @author Brian Clozel
 */
public class TriePathRouter implements RouteGroups {

	private final char separator;

	private final PatternsTrie<PathPattern> trie = new PatternsTrie<>(4);

	public TriePathRouter(char separator) {
		this.separator = separator;
	}

	public void add(List<String> rawPatterns) {
		PathPatternParser parser = new PathPatternParser();
		parser.setSeparator(this.separator);
		rawPatterns.stream()
				.forEach(p -> {
					this.trie.insert(PathContainer.parsePath(p, String.valueOf(this.separator)), parser.parse(p));
				});
	}

	@Override
	public List<PathPattern> findCandidates(String path) {
		PathContainer pathContainer = PathContainer.parsePath(path);
		List<PathPattern> matching = new ArrayList<>();
		for (PathPattern candidate : this.trie.selectMatches(pathContainer)) {
			if (candidate.matches(pathContainer)) {
				matching.add(candidate);
			}
		}
		return matching;
	}
}
