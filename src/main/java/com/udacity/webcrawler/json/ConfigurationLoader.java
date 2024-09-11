package com.udacity.webcrawler.json;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A utility class for loading a configuration from a JSON file.
 */
public final class ConfigurationLoader {

  private final Path configPath;

  /**
   * Constructs a {@link ConfigurationLoader} to load configurations from the specified {@link Path}.
   *
   * @param configPath the path to the configuration file.
   */
  public ConfigurationLoader(Path configPath) {
    this.configPath = Objects.requireNonNull(configPath, "Configuration path cannot be null.");
  }

  /**
   * Loads the configuration from the specified file path.
   *
   * @return a {@link CrawlerConfiguration} object containing the loaded configuration, or null if an error occurs.
   */
  public CrawlerConfiguration load() {
    // Attempt to read from the provided path
    try (Reader configReader = Files.newBufferedReader(configPath)) {
      return read(configReader);
    } catch (IOException e) {
      // Handle exception by returning null (or log error as needed)
      return null;
    }
  }

  /**
   * Reads the crawler configuration from the provided {@link Reader}.
   *
   * @param reader a {@link Reader} pointing to a JSON configuration string.
   * @return the loaded {@link CrawlerConfiguration}, or null if an error occurs.
   */
  public static CrawlerConfiguration read(Reader reader) {
    Objects.requireNonNull(reader, "Reader cannot be null.");
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(Feature.AUTO_CLOSE_SOURCE, false);

    try {
      // Read the JSON into a CrawlerConfiguration object
      return objectMapper.readValue(reader, CrawlerConfiguration.class);
    } catch (IOException e) {
      // Handle exception, possibly return null or log error
      return null;
    }
  }
}
