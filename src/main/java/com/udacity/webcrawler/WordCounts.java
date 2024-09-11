package com.udacity.webcrawler;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class that sorts the map of word counts.
 */
final class WordCounts {

	/**
	 * Given an unsorted map of word counts, returns a new map whose word counts are
	 * sorted according to the provided {@link WordCountComparator}, and includes
	 * only the top {@param popularWordCount} words and counts.
	 *
	 * <p>
	 * Reimplemented using only the Stream API and lambdas.
	 *
	 * @param wordCounts       the unsorted map of word counts.
	 * @param popularWordCount the number of popular words to include in the result
	 *                         map.
	 * @return a map containing the top {@param popularWordCount} words and counts
	 *         in the right order.
	 */
	static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {
		return wordCounts.entrySet().stream()
				// Sorting using Comparator logic directly within stream
				.sorted(Comparator.comparing((Map.Entry<String, Integer> entry) -> entry.getValue()).reversed()
						.thenComparing(entry -> entry.getKey().length(), Comparator.reverseOrder())
						.thenComparing(Map.Entry::getKey))
				.limit(popularWordCount)
				// Collecting results into a LinkedHashMap to maintain order
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						LinkedHashMap::new));
	}

	/**
	 * A {@link Comparator} that sorts word count pairs correctly:
	 *
	 * <ol>
	 * <li>First sorting by word count, ranking more frequent words higher.</li>
	 * <li>Then sorting by word length, ranking longer words higher.</li>
	 * <li>Finally, breaking ties using alphabetical order.</li>
	 * </ol>
	 */
	private static final class WordCountComparator implements Comparator<Map.Entry<String, Integer>> {
		@Override
		public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
			if (!a.getValue().equals(b.getValue())) {
				return b.getValue() - a.getValue();
			}
			if (a.getKey().length() != b.getKey().length()) {
				return b.getKey().length() - a.getKey().length();
			}
			return a.getKey().compareTo(b.getKey());
		}
	}

	private WordCounts() {
		// This class cannot be instantiated
	}
}
