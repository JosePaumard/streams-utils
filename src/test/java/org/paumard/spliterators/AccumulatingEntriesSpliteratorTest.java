/*
 * Copyright (C) 2017 José Paumard
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

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class AccumulatingEntriesSpliteratorTest {

    @Test
    public void should_accumulate_an_empty_entry_stream_into_an_empty_entry_stream() {
        // Given
        Stream<Map.Entry<Integer, String>> entries =
                Stream.of(
                        new AbstractMap.SimpleEntry<>(1, "1"),
                        new AbstractMap.SimpleEntry<>(2, "2")
                );
        entries = entries.filter(e -> e.getValue().length() > 10); // trick to create an empty ordered stream

        // When
        Stream<Map.Entry<Integer, String>> accumulate = StreamsUtils.accumulateEntries(entries, String::concat);

        // Then
        assertThat(accumulate.count()).isEqualTo(0L);
    }

    @Test
    public void should_accumulate_a_singleton_entry_stream_into_the_same_entry_stream() {
        // Given
        Stream<Map.Entry<Integer, String>> entries =
                Stream.of(
                        new AbstractMap.SimpleEntry<>(1, "1")
                );

        // When
        Stream<Map.Entry<Integer, String>> accumulate = StreamsUtils.accumulateEntries(entries, String::concat);

        // Then
        assertThat(accumulate.collect(toList())).containsExactly(new AbstractMap.SimpleEntry<>(1, "1"));
    }

    @Test
    public void should_accumulate_an_entry_stream_into_the_correct_entry_stream() {
        // Given
        Stream<Map.Entry<Integer, String>> entries =
                Stream.of(
                        new AbstractMap.SimpleEntry<>(1, "1"),
                        new AbstractMap.SimpleEntry<>(2, "2"),
                        new AbstractMap.SimpleEntry<>(3, "3"),
                        new AbstractMap.SimpleEntry<>(4, "4")
                );

        // When
        Stream<Map.Entry<Integer, String>> accumulate = StreamsUtils.accumulateEntries(entries, String::concat);

        // Then
        assertThat(accumulate.collect(toList())).containsExactly(
                new AbstractMap.SimpleEntry<>(1, "1"),
                new AbstractMap.SimpleEntry<>(2, "12"),
                new AbstractMap.SimpleEntry<>(3, "123"),
                new AbstractMap.SimpleEntry<>(4, "1234")
        );
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_an_accumulate_stream_on_a_null_stream() {

        StreamsUtils.accumulateEntries(null, Integer::sum);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_an_accumulate_stream_on_a_null_operator() {

        StreamsUtils.accumulateEntries(
                Stream.of(
                        new AbstractMap.SimpleEntry<>(1, "1"),
                        new AbstractMap.SimpleEntry<>(2, "2")
                ),
                null);
    }
}