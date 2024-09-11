package com.udacity.webcrawler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on
 * a {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
	private final Clock clock;
	private final Duration timeout;
	private final int popularWordCount;
	private final ForkJoinPool pool;
	private final int maxDepth;
	private final List<Pattern> ignoredUrls;
	private final PageParserFactory parserFactory;

	@Inject
	ParallelWebCrawler(Clock clock, PageParserFactory parserFactory, @Timeout Duration timeout,
			@PopularWordCount int popularWordCount, @TargetParallelism int threadCount, @MaxDepth int maxDepth,
			@IgnoredUrls List<Pattern> ignoredUrls) {
		this.clock = clock;
		this.timeout = timeout;
		this.popularWordCount = popularWordCount;
		this.maxDepth = maxDepth;
		this.ignoredUrls = ignoredUrls;
		this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
		this.parserFactory = parserFactory;
	}

	@Override
	public CrawlResult crawl(List<String> startingUrls) {
		Instant deadline = clock.instant().plus(timeout);
		ConcurrentMap<String, Integer> counts = new ConcurrentHashMap<>();
		ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();

		for (String url : startingUrls) {
			pool.invoke(new CrawlRecursiveTask(url, deadline, maxDepth, counts, visitedUrls));
		}

		return buildCrawlResult(counts, visitedUrls);
	}

	// Private helper method to build the CrawlResult
	private CrawlResult buildCrawlResult(ConcurrentMap<String, Integer> counts,
			ConcurrentSkipListSet<String> visitedUrls) {
		if (counts.isEmpty()) {
			return new CrawlResult.Builder().setWordCounts(counts).setUrlsVisited(visitedUrls.size()).build();
		}
		return new CrawlResult.Builder().setWordCounts(WordCounts.sort(counts, popularWordCount))
				.setUrlsVisited(visitedUrls.size()).build();
	}

	@Override
	public int getMaxParallelism() {
		return Runtime.getRuntime().availableProcessors();
	}

	public class CrawlRecursiveTask extends RecursiveTask<Void> {
		private final String url;
		private final Instant deadline;
		private final int maxDepth;
		private final ConcurrentMap<String, Integer> counts;
		private final ConcurrentSkipListSet<String> visitedUrls;

		private CrawlRecursiveTask(String url, Instant deadline, int maxDepth, ConcurrentMap<String, Integer> counts,
				ConcurrentSkipListSet<String> visitedUrls) {
			this.url = url;
			this.deadline = deadline;
			this.maxDepth = maxDepth;
			this.counts = counts;
			this.visitedUrls = visitedUrls;
		}

		@Override
		protected Void compute() {
			if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
				return null;
			}

			if (isIgnoredOrVisited()) {
				return null;
			}

			PageParser.Result result = parserFactory.get(url).parse();
			updateWordCounts(result);
			createAndInvokeSubtasks(result.getLinks());

			return null;
		}

		// Helper method to check if URL is ignored or already visited
		private boolean isIgnoredOrVisited() {
			for (Pattern pattern : ignoredUrls) {
				if (pattern.matcher(url).matches()) {
					return true;
				}
			}
			return !visitedUrls.add(url);
		}

		// Helper method to update word counts from the parsed result
		private void updateWordCounts(PageParser.Result result) {
			result.getWordCounts().forEach((word, count) -> counts.merge(word, count, Integer::sum));
		}

		// Helper method to create and invoke subtasks for links
		private void createAndInvokeSubtasks(List<String> links) {
			List<CrawlRecursiveTask> subtasks = new ArrayList<>();
			for (String link : links) {
				subtasks.add(new CrawlRecursiveTask(link, deadline, maxDepth - 1, counts, visitedUrls));
			}
			invokeAll(subtasks);
		}
	}
}
