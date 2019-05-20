package io.spring.sample;

import java.util.List;

import org.springframework.web.util.pattern.PathPattern;

public interface RouteGroups {

	List<PathPattern> findCandidates(String path);
}
