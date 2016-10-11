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

import java.util.List;
import java.util.Map;
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
public class CrossingSpliteratorTest {

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
    public void should_cross_a_non_empty_stream_into_a_stream_of_entries() {
        // Given
        Stream<String> strings = Stream.of("a", "b", "c", "d");

        // When
        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProductNaturallyOrdered(strings);
        List<Map.Entry<String, String>> entries = stream.collect(toList());

        // Then
        assertThat(entries.size()).isEqualTo(6);
        assertThat(entries.get(0).getKey()).isEqualTo("b");
        assertThat(entries.get(0).getValue()).isEqualTo("a");
        assertThat(entries.get(1).getKey()).isEqualTo("c");
        assertThat(entries.get(1).getValue()).isEqualTo("a");
        assertThat(entries.get(2).getKey()).isEqualTo("c");
        assertThat(entries.get(2).getValue()).isEqualTo("b");
        assertThat(entries.get(3).getKey()).isEqualTo("d");
        assertThat(entries.get(3).getValue()).isEqualTo("a");
        assertThat(entries.get(4).getKey()).isEqualTo("d");
        assertThat(entries.get(4).getValue()).isEqualTo("b");
        assertThat(entries.get(5).getKey()).isEqualTo("d");
        assertThat(entries.get(5).getValue()).isEqualTo("c");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_crossing_spliterator_on_a_null_spliterator() {

        Stream<Map.Entry<String, String>> stream = StreamsUtils.crossProductOrdered(null, null);
    }
}