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

import org.paumard.spliterators.util.TryAdvanceCheckingSpliterator;
import org.paumard.streams.StreamsUtils;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Created by José
 */
public class CrossProductSpliteratorTest {

    @Test
    public void should_cross_an_empty_stream_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();

        // When
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProduct(strings);
        long count = stream.count();

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    public void should_cross_a_singleton_stream_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.of("a");

        // When
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProduct(strings);
        List<Map.Entry<String, String>> entries = stream.collect(toList());

        // Then
        assertThat(entries.size()).isEqualTo(1);
        Map.Entry<String, String> entry = entries.iterator().next();
        assertThat(entry.getKey()).isEqualTo("a");
        assertThat(entry.getValue()).isEqualTo("a");
    }

    @Test
    public void should_cross_a_non_empty_stream_into_a_stream_of_entries() {
        // Given
        Stream<String> strings = Stream.of("a", "b", "c", "d");


        // When
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProduct(strings);
        Comparator<Map.Entry<String, String>> comparator =
                Comparator.<Map.Entry<String, String>, String>comparing(Map.Entry::getKey)
                        .thenComparing(Map.Entry::getValue);
        Set<Map.Entry<String, String>> entries =
                stream.collect(
                        Collectors.toCollection(() -> new TreeSet<>(comparator))
                );

        // Then
        assertThat(entries.size()).isEqualTo(16);
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
        Map.Entry<String, String> entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("a");
        assertThat(entry.getValue()).isEqualTo("a");
        entry = iterator.next();
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
        assertThat(entry.getValue()).isEqualTo("a");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("b");
        assertThat(entry.getValue()).isEqualTo("b");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("b");
        assertThat(entry.getValue()).isEqualTo("c");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("b");
        assertThat(entry.getValue()).isEqualTo("d");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("c");
        assertThat(entry.getValue()).isEqualTo("a");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("c");
        assertThat(entry.getValue()).isEqualTo("b");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("c");
        assertThat(entry.getValue()).isEqualTo("c");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("c");
        assertThat(entry.getValue()).isEqualTo("d");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("d");
        assertThat(entry.getValue()).isEqualTo("a");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("d");
        assertThat(entry.getValue()).isEqualTo("b");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("d");
        assertThat(entry.getValue()).isEqualTo("c");
        entry = iterator.next();
        assertThat(entry.getKey()).isEqualTo("d");
        assertThat(entry.getValue()).isEqualTo("d");
    }

    @Test
    public void should_be_able_to_cross_product_a_sorted_stream_in_an_non_sorted_cross_product_stream() {
        // Given
        SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList("one", "two", "three"));

        // When
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProduct(sortedSet.stream());

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("a", "d", "c", "b");
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProduct(strings);
        TryAdvanceCheckingSpliterator<Map.Entry<String, String>> spliterator = new TryAdvanceCheckingSpliterator<>(stream.spliterator());
        Stream<Map.Entry<String, String>> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(16L);
    }

    @Test
    public void should_correctly_count_the_elements_of_a_sized_stream() {
        // Given
        Stream<String> strings = Stream.of("a", "d", "c", "b");
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProduct(strings);

        // When
        long count = stream.count();

        // Then
        assertThat(count).isEqualTo(16L);
    }

    @Test
    public void should_correctly_call_the_onClose_callbacks_of_the_underlying_streams() {
        // Given
        AtomicBoolean b = new AtomicBoolean(false);
        Stream<String> strings = Stream.of("a", "d", "c", "b").onClose(() -> b.set(true));

        // When
        StreamsUtils.crossProduct(strings).close();

        // Then
        assertThat(b.get()).isEqualTo(true);
    }

    @Test
    public void should_not_build_a_crossing_spliterator_on_a_null_spliterator() {
        // Then
        assertThatNullPointerException().isThrownBy(() -> StreamsUtils.crossProduct(null));
    }
}