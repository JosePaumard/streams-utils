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

import org.paumard.spliterators.util.TryAdvanceCheckingSpliterator;
import org.paumard.streams.StreamsUtils;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Created by José
 */
public class FilteringAllMaxSpliteratorTest {

    @Test
    public void should_filter_an_empty_stream_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        long n = StreamsUtils.filteringAllMax(strings, comparator).count();
        // Then
        assertThat(n).isEqualTo(0L);
    }

    @Test
    public void should_filter_a_non_empty_stream_into_a_the_right_stream_when_several_maxes() {
        // Given
        Stream<String> strings = Stream.of("1", "1", "2", "2", "3", "3");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringAllMax(strings, comparator).collect(toList());

        // Then
        assertThat(list).containsExactly("3", "3");
    }

    @Test
    public void should_filter_a_non_empty_stream_into_a_the_right_stream_when_one_max() {
        // Given
        Stream<String> strings = Stream.of("4", "1", "1", "2", "2", "3", "3");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringAllMax(strings, comparator).collect(toList());

        // Then
        assertThat(list).containsExactly("4");
    }

    @Test
    public void should_be_able_to_filter_all_maxes_of_a_sorted_stream_in_an_sorted_filtered_stream() {
        // Given
        SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList("one", "two", "three"));

        // When
        Stream<String> stream = StreamsUtils.filteringAllMax(sortedSet.stream());

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(Spliterator.SORTED);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Arrays.asList("one", "two", "three").stream();
        Stream<String> testedStream = StreamsUtils.filteringAllMax(strings);
        TryAdvanceCheckingSpliterator<String> spliterator = new TryAdvanceCheckingSpliterator<>(testedStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void should_correctly_count_the_elements_of_a_sized_stream() {
        // Given
        Stream<String> strings = Arrays.asList("one", "two", "three").stream();
        Stream<String> stream = StreamsUtils.filteringAllMax(strings);

        // When
        long count = stream.count();

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void should_correctly_call_the_onClose_callbacks_of_the_underlying_streams() {
        // Given
        AtomicBoolean b = new AtomicBoolean(false);
        Stream<String> strings = Stream.of("one", "two", "three").onClose(() -> b.set(true));

        // When
        StreamsUtils.filteringAllMax(strings).close();

        // Then
        assertThat(b.get()).isEqualTo(true);
    }

    @Test
    public void should_not_build_a_filtering_spliterator_on_a_null_stream() {
        // Given
        Comparator<String> comparator = Comparator.naturalOrder();

        // Then
        assertThatNullPointerException().isThrownBy(() -> StreamsUtils.filteringAllMax(null, comparator));
    }

    @Test
    public void should_not_build_a_filtering_spliterator_on_a_null_comparator() {
        // Given
        Stream<String> strings = Stream.of("1", "1", "2", "2", "3", "3");

        // When
        assertThatNullPointerException().isThrownBy(() -> StreamsUtils.filteringAllMax(strings, null).collect(toList()));
    }
}