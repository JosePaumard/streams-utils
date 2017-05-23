/*
 * Copyright (C) 2016 José Paumard
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by José
 */
public class CrossProductNaturalyOrderedSpliteratorTest {

    @Test
    public void should_cross_an_empty_stream_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();

        // When
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProductNaturallyOrdered(strings);
        long count = stream.count();

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    public void should_cross_a_singleton_stream_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.of("a");

        // When
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProductNaturallyOrdered(strings);
        List<Map.Entry<String, String>> entries = stream.collect(toList());

        // Then
        assertThat(entries.size()).isEqualTo(0);
    }

    @Test
    public void should_cross_a_non_empty_stream_into_a_naturaly_ordered_stream_of_entries() {
        // Given
        Stream<String> strings = Stream.of("a", "d", "c", "b");

        // When
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProductNaturallyOrdered(strings);
        Comparator<Map.Entry<String, String>> comparator =
                Comparator.<Map.Entry<String, String>, String>comparing(Map.Entry::getKey)
                        .thenComparing(Map.Entry::getValue);
        Set<Map.Entry<String, String>> entries =
                stream.collect(
                        Collectors.toCollection(() -> new TreeSet<>(comparator))
                );

        // Then
        assertThat(entries.size()).isEqualTo(6);
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
        Map.Entry<String, String> entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("a");
        assertThat(entry.getValue()).isEqualTo("b");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("a");
        assertThat(entry.getValue()).isEqualTo("c");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("a");
        assertThat(entry.getValue()).isEqualTo("d");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("b");
        assertThat(entry.getValue()).isEqualTo("c");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("b");
        assertThat(entry.getValue()).isEqualTo("d");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("c");
        assertThat(entry.getValue()).isEqualTo("d");
    }

    @Test
    public void should_cross_a_non_empty_stream_into_a_stream_of_entries_using_a_comparator() {
        // Given
        Stream<String> strings = Stream.of("a", "d", "c", "b");

        // When
        Stream<Map.Entry<String, String>> stream =
                StreamsUtils.crossProductOrdered(strings, Comparator.<String>naturalOrder().reversed());
        Comparator<Map.Entry<String, String>> comparator =
                Comparator.<Map.Entry<String, String>, String>comparing(Map.Entry::getKey)
                        .thenComparing(Map.Entry::getValue);
        Set<Map.Entry<String, String>> entries =
                stream.collect(
                        Collectors.toCollection(() -> new TreeSet<>(comparator))
                );

        // Then
        assertThat(entries.size()).isEqualTo(6);
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
        Map.Entry<String, String> entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("b");
        assertThat(entry.getValue()).isEqualTo("a");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("c");
        assertThat(entry.getValue()).isEqualTo("a");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("c");
        assertThat(entry.getValue()).isEqualTo("b");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("d");
        assertThat(entry.getValue()).isEqualTo("a");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("d");
        assertThat(entry.getValue()).isEqualTo("b");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("d");
        assertThat(entry.getValue()).isEqualTo("c");
    }


    @Test
    public void should_be_able_to_cross_product_a_sorted_stream_in_an_non_sorted_stream() {
        // Given
        SortedMap<Long, String> sortedMap = new TreeMap<>();
        sortedMap.put(1L, "ONE");
        sortedMap.put(2L, "TWO");
        sortedMap.put(3L, "THREE");

        // When
        Stream<Map.Entry<Map.Entry<Long, String>, Map.Entry<Long, String>>> stream =
                StreamsUtils.crossProductOrdered(sortedMap.entrySet().stream(), Map.Entry.comparingByKey());

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_crossing_spliterator_on_a_null_spliterator() {

        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProductOrdered(null, null);
    }
}