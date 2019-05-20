package io.spring.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import org.springframework.util.Assert;
import org.springframework.web.util.pattern.PathPattern;

public class TrieMatchingBenchmark {

	@State(Scope.Benchmark)
	static public class RoutingState {

		List<Route> routes;

		IteratingPathRouter iteratingPathRouter;

		TriePathRouter triePathRouter;

		String lookupPath;

		Random random;

		@Setup(Level.Iteration)
		public void setup() {
			this.routes = RouteGenerator.generateMicroRoutes();
			List<String> patterns = this.routes.stream().map(Route::getPattern).collect(Collectors.toList());
			this.iteratingPathRouter = new IteratingPathRouter(patterns);
			this.triePathRouter = new TriePathRouter('/');
			this.triePathRouter.add(patterns);
		}

		@Setup(Level.Invocation)
		public void prepare() {
			this.random = new Random();
			int index = this.random.nextInt(this.routes.size());
			this.lookupPath = this.routes.get(index).generateMatchingPath();
		}

		@TearDown(Level.Iteration)
		public void shutdown() {
			this.iteratingPathRouter = null;
			this.triePathRouter = null;
		}
	}

	@Benchmark
	public void baseline(Blackhole blackHole, RoutingState routingState) {

		List<Route> matching = new ArrayList<>();
		for (Route route : routingState.routes) {
			blackHole.consume(route.getPattern());
			blackHole.consume(routingState.lookupPath);
		}
		blackHole.consume(matching);
	}

	@Benchmark
	public void iteratingRouter(Blackhole blackHole, RoutingState routingState) {
		RouteGroups router = routingState.iteratingPathRouter;
		blackHole.consume(router.findCandidates(routingState.lookupPath));

	}

	@Benchmark
	public void trieRouter(Blackhole blackHole, RoutingState routingState) {
		RouteGroups router = routingState.triePathRouter;
		blackHole.consume(router.findCandidates(routingState.lookupPath));
	}

	public static void main(String[] args) throws RunnerException {
		testImplementations();
		/*
		Options opt = new OptionsBuilder()
				.include(TrieMatchingBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.warmupTime(TimeValue.seconds(10))
				.measurementIterations(5)
				.measurementTime(TimeValue.seconds(5))
				.forks(2)
				.warmupForks(2)
				.addProfiler("gc")
				.mode(Mode.Throughput)
				.build();

		new Runner(opt).run();
		 */
	}

	private static void testImplementations() {
		List<Route> routes = RouteGenerator.generateSaganRoutes();
		List<String> patterns = routes.stream().map(Route::getPattern).collect(Collectors.toList());
		IteratingPathRouter iteratingPathRouter = new IteratingPathRouter(patterns);
		TriePathRouter triePathRouter = new TriePathRouter('/');
		triePathRouter.add(patterns);
		for (Route route : routes) {
			String path = route.generateMatchingPath();
			List<PathPattern> iteratingResult = iteratingPathRouter.findCandidates(path);
			List<PathPattern> trieResult = triePathRouter.findCandidates(path);
			for (PathPattern pattern : iteratingResult) {
				Assert.isTrue(trieResult.contains(pattern),
						"Pattern " + pattern.toString() + " missing from " + trieResult + "with path " + path);
			}
		}
	}


	private interface Route {

		String getPattern();

		String generateMatchingPath();
	}

	private static class RouteGenerator {

		static List<Route> generateSaganRoutes() {
			List<Route> routes = new ArrayList<>();

			routes.add(new StaticRoute("/404"));
			routes.add(new StaticRoute("/500"));
			routes.add(new StaticRoute("/platform"));
			routes.add(new StaticRoute("/services"));
			routes.add(new StaticRoute("/signin"));
			routes.add(new StaticRoute("/**"));
			routes.add(new StaticRoute("/blog.atom"));
			routes.add(new DynamicRoute("/blog/category/{category}.atom",
					"/blog/category/releases.atom",
					"/blog/category/engineering.atom",
					"/blog/category/news.atom"));
			routes.add(new StaticRoute("/blog/broadcasts.atom"));
			routes.add(new StaticRoute("/blog"));
			routes.add(new DynamicRoute("/blog/{year:\\\\d+}/{month:\\\\d+}/{day:\\\\d+}/{slug}",
					"/blog/2017/02/09/spring-cloud-pipelines-1-0-0-m3-released",
					"/blog/2017/02/06/springone-platform-2016-replay-spring-for-apache-kafka",
					"/blog/2017/02/06/spring-for-apache-kafka-1-1-3-available-now",
					"/blog/2017/02/06/spring-cloud-camden-sr5-is-available",
					"/blog/2017/02/01/spring-team-at-devnexus-2017",
					"/blog/2017/02/01/spring-io-platform-athens-sr3"));
			routes.add(new StaticRoute("/blog/broadcasts"));
			routes.add(new DynamicRoute("/blog/{year:\\\\d+}/{month:\\\\d+}",
					"/blog/2017/02", "/blog/2017/01", "/blog/2016/12", "/blog/2016/11"));
			routes.add(new StaticRoute("/docs/reference"));
			routes.add(new StaticRoute("/docs"));
			routes.add(new StaticRoute("/webhook/docs/guides"));
			routes.add(new DynamicRoute("/webhook/docs/guides/{repositoryName}",
					"/webhook/docs/guides/rest-service", "/webhook/docs/guides/scheduling-tasks",
					"/webhook/docs/guides/consuming-rest", "/webhook/docs/guides/relational-data-access"));
			routes.add(new DynamicRoute("/guides/gs/{repositoryName}",
					"/guides/gs/rest-service", "/guides/gs/scheduling-tasks",
					"/guides/gs/consuming-rest", "/guides/gs/relational-data-access"));
			routes.add(new DynamicRoute("/guides/tut/{repositoryName}", "/guide/tut/spring-security",
					"/guides/tut/kotlin", "/guides/tut/how-to-benchmark"));
			routes.add(new StaticRoute("/error"));
			routes.add(new StaticRoute("/tools"));
			routes.add(new StaticRoute("/tools/eclipse"));
			routes.add(new StaticRoute("/tools/sts"));
			routes.add(new StaticRoute("/tools/sts/all"));
			routes.add(new DynamicRoute("/team/{username}",
					"/team/bclozel", "/team/snicoll", "/team/sdeleuze", "/team/rstoyanchev"));
			routes.add(new StaticRoute("/team"));
			routes.add(new StaticRoute("/search"));
			routes.add(new StaticRoute("/project"));
			routes.add(new StaticRoute("/questions"));
			routes.add(new DynamicRoute("/projects/{projectId}",
					"/projects/spring-boot", "/projects/spring-framework",
					"/projects/reactor", "/projects/spring-data",
					"/projects/spring-restdocs", "/projects/spring-batch"));
			routes.add(new DynamicRoute("/badges/{projectId}.svg",
					"/badges/spring-boot.svg", "/badges/spring-framework.svg",
					"/badges/reactor", "/badges/spring-data.svg",
					"/badges/spring-restdocs.svg", "/badges/spring-batch.svg"));
			return routes;
		}

		static List<Route> generateMicroRoutes() {
			List<Route> routes = new ArrayList<>();

			routes.add(new StaticRoute("/"));
			routes.add(new StaticRoute("/**"));
			routes.add(new DynamicRoute("/users/{name}",
					"/users/brian",
					"/users/stephane", "/users/rossen"));
			routes.add(new DynamicRoute("/projects/{projectId}",
					"/projects/spring-boot", "/projects/spring-framework",
					"/projects/reactor", "/projects/spring-data",
					"/projects/spring-restdocs", "/projects/spring-batch"));
			routes.add(new DynamicRoute("/products/{id}",
					"/products/12", "/products/13",
					"/products/14", "/products/15",
					"/products/16", "/products/17"));
			return routes;
		}

	}

	private static class StaticRoute implements Route {

		private final String pattern;

		public StaticRoute(String route) {
			pattern = route;
		}

		public String getPattern() {
			return pattern;
		}

		public String generateMatchingPath() {
			return this.pattern;
		}

	}

	private static class DynamicRoute implements Route {

		private final String pattern;

		private final List<String> matchingPaths;

		public DynamicRoute(String pattern, String... matchingPaths) {
			this.pattern = pattern;
			this.matchingPaths = Arrays.asList(matchingPaths);
		}

		public String getPattern() {
			return pattern;
		}

		public String generateMatchingPath() {
			Random random = new Random();
			return this.matchingPaths.get(random.nextInt(this.matchingPaths.size()));
		}

	}
}
