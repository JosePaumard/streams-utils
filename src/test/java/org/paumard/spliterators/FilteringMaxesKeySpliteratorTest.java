/*
 * Copyright (C) 2015 José Paumard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.paumard.spliterators;

import org.paumard.streams.StreamsUtils;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by José
 */
public class FilteringMaxesKeySpliteratorTest {

    @Test
    public void should_filter_an_empty_stream_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        long n = StreamsUtils.filteringMaxesKeys(strings, 2, comparator).count();
        // Then
        assertThat(n).isEqualTo(0L);
    }

    @Test
    public void should_filter_a_non_empty_stream_into_the_right_stream_when_no_duplicates() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(strings, 2, comparator).collect(toList());

        // Then
        assertThat(list).asList().containsExactly("3", "4");
    }

    @Test
    public void should_filter_a_non_empty_stream_into_the_right_stream_when_no_duplicates_and_shuffle() {
        // Given
        Stream<String> strings = Stream.of("4", "1", "2", "3");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(strings, 2, comparator).collect(toList());

        // Then
        assertThat(list).asList().containsExactly("3", "4");
    }

    @Test
    public void should_filter_a_non_empty_stream_into_the_right_stream_when_no_duplicates_and_a_greater_number_of_maxes() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(strings, 10, comparator).collect(toList());

        // Then
        assertThat(list).asList().containsExactly("1", "2", "3");
    }

    @Test
    public void should_filter_a_non_empty_stream_into_the_right_stream_when_no_duplicates_and_a_greater_number_of_maxes_and_shuffle() {
        // Given
        Stream<String> strings = Stream.of("3", "1", "2");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(strings, 10, comparator).collect(toList());

        // Then
        assertThat(list).asList().containsExactly("1", "2", "3");
    }

    @Test
    public void should_filter_a_non_empty_stream_into_the_right_stream_when_duplicates() {
        // Given
        Stream<String> strings = Stream.of("1", "1", "2", "2", "2", "3", "3", "4", "4", "4");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(strings, 2, comparator).collect(toList());

        // Then
        assertThat(list).asList().containsExactly("4", "4", "4");
    }

    @Test
    public void should_filter_a_non_empty_stream_into_the_right_stream_with_4_and_duplicates() {
        // Given
        Stream<String> strings = Stream.of("1", "1", "2", "2", "2", "3", "3", "4", "4", "4");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(strings, 4, comparator).collect(toList());

        // Then
        assertThat(list).asList().containsExactly("3", "3", "4", "4", "4");
    }

    @Test
    public void should_filter_a_non_empty_stream_into_the_right_stream_with_7_and_duplicates() {
        // Given
        Stream<String> strings = Stream.of("2", "1", "3", "4", "1", "2", "3", "2", "4", "4");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(strings, 7, comparator).collect(toList());

        // Then
        assertThat(list).asList().containsExactly("2", "2", "2", "3", "3", "4", "4", "4");
    }

    @Test
    public void should_filter_a_non_empty_stream_into_the_right_stream_when_duplicates_shuffle() {
        // Given
        Stream<String> strings = Stream.of("2", "1", "3", "4", "1", "2", "3", "2", "4", "4");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(strings, 2, comparator).collect(toList());

        // Then
        assertThat(list).asList().containsExactly("4", "4", "4");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_filtering_spliterator_on_a_null_stream() {

        // Given
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(null, 10, comparator).collect(toList());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_filtering_spliterator_on_a_null_comparator() {

        // Given
        Stream<String> strings = Stream.of("1", "1", "2", "2", "3", "3");

        // When
        List<String> list = StreamsUtils.filteringMaxesKeys(strings, 10, null).collect(toList());
    }
}