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

import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static org.paumard.streams.StreamsUtils.cycle;
import static org.paumard.streams.StreamsUtils.zip;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class CyclingSpliteratorTest {

    @Test
    public void should_cycle_an_empty_stream_into_a_stream_of_streams() {
        // Given
        Stream<String> strings = Stream.empty();

        // When
        long n = StreamSupport.stream(CyclingSpliterator.of(strings.spliterator()), false).limit(10).count();
        // Then
        assertThat(n).isEqualTo(10);
    }

    @Test
    public void should_cycle_an_empty_stream_into_a_stream_of_empty_streams() {
        // Given
        Stream<String> strings = Stream.empty();

        // When
        Stream<String> stream = StreamSupport.stream(CyclingSpliterator.of(strings.spliterator()), false).limit(10).flatMap(identity());
        // Then
        assertThat(stream.count()).isEqualTo(0);
    }

    @Test
    public void should_cycle_a_non_empty_stream_into_a_stream_of_streams() {
        // Given
        Stream<String> strings = Stream.of("one", "two");

        // When
        long n = StreamSupport.stream(CyclingSpliterator.of(strings.spliterator()), false).limit(10).count();
        // Then
        assertThat(n).isEqualTo(10);
    }

    @Test
    public void should_cycle_a_non_empty_stream_into_a_stream_of_non_empty_streams() {
        // Given
        Stream<String> strings = Stream.of("one", "two");

        // When
        List<String> list  = StreamSupport.stream(CyclingSpliterator.of(strings.spliterator()), false).limit(3).flatMap(identity()).collect(toList());
        // Then
        assertThat(list).containsExactly("one", "two", "one", "two", "one", "two");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_cycling_spliterator_on_a_null_spliterator() {

        CyclingSpliterator<String> cyclingSpliterator = CyclingSpliterator.of(null);
    }

    @Test
    public void should_solve_fizzbuzz_correctly() {

        // When
        Stream<String> fizzBuzz =
                zip(
                        IntStream.range(0, 101).boxed(),
                        zip(
                                cycle(Stream.of("fizz", "", "")),
                                cycle(Stream.of("buzz", "", "", "", "")),
                                String::concat
                        ),
                        (i, string) -> string.isEmpty() ? i.toString() : string
                );
        List<String> fizzBuzzList = fizzBuzz.skip(1).limit(20).collect(toList());

        // Then
        assertThat(fizzBuzzList).containsExactly(
                         "1",    "2", "fizz",  "4",     "buzz", "fizz",  "7",    "8", "fizz",
                "buzz", "11", "fizz",   "13", "14", "fizzbuzz",   "16", "17", "fizz",   "19",
                "buzz");
    }
}