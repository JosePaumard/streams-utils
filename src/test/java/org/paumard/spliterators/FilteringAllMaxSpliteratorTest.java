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
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.paumard.streams.StreamsUtils.cycle;
import static org.paumard.streams.StreamsUtils.zip;

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
    public void should_filter_a_non_empty_stream_into_a_the_right_stream() {
        // Given
        Stream<String> strings = Stream.of("1", "1", "2", "2", "3", "3");
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringAllMax(strings, comparator).collect(toList());

        // Then
        assertThat(list).asList().containsExactly("3", "3");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_filtering_spliterator_on_a_null_stream() {

        // Given
        Comparator<String> comparator = Comparator.naturalOrder();

        // When
        List<String> list = StreamsUtils.filteringAllMax(null, comparator).collect(toList());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_filtering_spliterator_on_a_null_comparator() {

        // Given
        Stream<String> strings = Stream.of("1", "1", "2", "2", "3", "3");

        // When
        List<String> list = StreamsUtils.filteringAllMax(strings, null).collect(toList());
    }
}