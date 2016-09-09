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

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by José
 */
public class ZippingSpliteratorTest {

    @Test
    public void should_zip_empty_streams_into_a_stream_of_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();
        Stream<Integer> ints = Stream.empty();
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        ZippingSpliterator<String, Integer, String> zippingSpliterator =
                new ZippingSpliterator.Builder<String, Integer, String>()
                        .with(strings.spliterator())
                        .and(ints.spliterator())
                        .mergedBy(zip)
                        .build();
        long count = StreamSupport.stream(zippingSpliterator, false).count();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void should_zip_two_correct_streams_together() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Stream<Integer> ints = Stream.of(1, 2, 3);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        ZippingSpliterator<String, Integer, String> zippingSpliterator =
                new ZippingSpliterator.Builder<String, Integer, String>()
                        .with(strings.spliterator())
                        .and(ints.spliterator())
                        .mergedBy(zip)
                        .build();
        List<String> list = StreamSupport.stream(zippingSpliterator, false).collect(toList());

        // Then
        Assertions.assertThat(list.size()).isEqualTo(3);
        Assertions.assertThat(list.get(0)).isEqualTo("one - 1");
        Assertions.assertThat(list.get(1)).isEqualTo("two - 2");
        Assertions.assertThat(list.get(2)).isEqualTo("three - 3");
    }

    @Test
    public void should_zip_two_streams_together_when_the_first_is_too_long() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three", "four");
        Stream<Integer> ints = Stream.of(1, 2, 3);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        ZippingSpliterator<String, Integer, String> zippingSpliterator =
                new ZippingSpliterator.Builder<String, Integer, String>()
                        .with(strings.spliterator())
                        .and(ints.spliterator())
                        .mergedBy(zip)
                        .build();
        List<String> list = StreamSupport.stream(zippingSpliterator, false).collect(toList());

        // Then
        Assertions.assertThat(list.size()).isEqualTo(3);
        Assertions.assertThat(list.get(0)).isEqualTo("one - 1");
        Assertions.assertThat(list.get(1)).isEqualTo("two - 2");
        Assertions.assertThat(list.get(2)).isEqualTo("three - 3");
    }

    @Test
    public void should_zip_two_streams_together_when_the_second_is_too_long() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Stream<Integer> ints = Stream.of(1, 2, 3, 4);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        ZippingSpliterator<String, Integer, String> zippingSpliterator =
                new ZippingSpliterator.Builder<String, Integer, String>()
                        .with(strings.spliterator())
                        .and(ints.spliterator())
                        .mergedBy(zip)
                        .build();
        List<String> list = StreamSupport.stream(zippingSpliterator, false).collect(toList());

        // Then
        Assertions.assertThat(list.size()).isEqualTo(3);
        Assertions.assertThat(list.get(0)).isEqualTo("one - 1");
        Assertions.assertThat(list.get(1)).isEqualTo("two - 2");
        Assertions.assertThat(list.get(2)).isEqualTo("three - 3");
    }
}