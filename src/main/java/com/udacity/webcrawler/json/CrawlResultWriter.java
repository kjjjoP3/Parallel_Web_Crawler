package com.udacity.webcrawler.json;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class to write a {@link CrawlResult} to file.
 */
public final class CrawlResultWriter {
  private final CrawlResult result;

  /**
   * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
   */
  public CrawlResultWriter(CrawlResult result) {
    // Ensure the result object is not null
    this.result = Objects.requireNonNull(result);
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data should be appended to it.
   *
   * @param path the file path where the crawl result data should be written.
   */
  public void write(Path path) {
    // Ensure path is not null and handle file writing with proper options
    Objects.requireNonNull(path, "The provided path cannot be null.");
    try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      // Delegate to the write method that handles writing to a Writer
      write(writer);
    } catch (IOException e) {
      // Handle exceptions with a meaningful message
      System.err.println("Error writing to file: " + e.getMessage());
    }
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
   *
   * @param writer the destination where the crawl result data should be written.
   */
  public void write(Writer writer) {
    // Ensure writer is not null before processing
    Objects.requireNonNull(writer, "The writer cannot be null.");
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(Feature.AUTO_CLOSE_TARGET);
    try {
      // Write the CrawlResult object to the writer in JSON format
      objectMapper.writeValue(writer, result);
    } catch (IOException e) {
      // Handle any IO exceptions that occur during the writing process
      System.err.println("Error writing result to writer: " + e.getMessage());
    }
  }
}
