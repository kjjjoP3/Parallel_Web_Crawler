package com.udacity.webcrawler.main;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.inject.Inject;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

public final class WebCrawlerMain {

	private final CrawlerConfiguration config;

	private WebCrawlerMain(CrawlerConfiguration config) {
		// Ensure that configuration is not null
		this.config = Objects.requireNonNull(config, "Crawler configuration must not be null.");
	}

	@Inject
	private WebCrawler crawler;

	@Inject
	private Profiler profiler;

	private void run() throws Exception {
		// Set up dependency injection for web crawling and profiling modules
		Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

		// Execute the web crawling process based on start pages
		CrawlResult result = crawler.crawl(config.getStartPages());

		// Initialize a writer for the result and decide output destination
		CrawlResultWriter resultWriter = new CrawlResultWriter(result);

		// Determine whether to write results to a file or console
		String resultPath = config.getResultPath();
		if (resultPath != null && !resultPath.isEmpty()) {
			// If a path is provided, write the result to the specified path
			Path path = Paths.get(resultPath);
			resultWriter.write(path);
		} else {
			// Otherwise, write the result to standard output (console)
			try (Writer writer = new OutputStreamWriter(System.out)) {
				resultWriter.write(writer);
			}
		}

		// TODO: Write profile data to a file or console if no file path is provided
		String profileOutputPath = config.getProfileOutputPath();
		if (profileOutputPath != null && !profileOutputPath.isEmpty()) {
			// Handle profiling output here if a valid path is given
			profiler.writeData(Paths.get(profileOutputPath));
		} else {
			// Otherwise, output profiling data to the console
			profiler.writeData(new OutputStreamWriter(System.out));
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: WebCrawlerMain [starting-url]");
			return;
		}

		// Load the configuration from the provided file path
		CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
		// Run the crawler with the loaded configuration
		new WebCrawlerMain(config).run();
	}
}
