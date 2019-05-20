package io.spring.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @author Brian Clozel
 */
public class IteratingPathRouter implements RouteGroups {

	protected final List<PathPattern> patterns;

	public IteratingPathRouter(List<String> rawPatterns) {
		PathPatternParser parser = new PathPatternParser();
		this.patterns = rawPatterns.stream()
				.map(parser::parse)
				.collect(Collectors.toList());
	}

	@Override
	public List<PathPattern> findCandidates(String path) {
		List<PathPattern> matching = new ArrayList<>();
		PathContainer pathContainer = PathContainer.parsePath(path);
		for(PathPattern pattern : this.patterns) {
			if(pattern.matches(pathContainer)) {
				matching.add(pattern);
			}
		}
		return matching;
	}
}
