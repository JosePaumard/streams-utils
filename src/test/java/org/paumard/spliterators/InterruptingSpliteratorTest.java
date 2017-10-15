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
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class InterruptingSpliteratorTest {

    @Test
    public void should_interrupt_an_empty_streams_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();
        Predicate<String> interruptor = String::isEmpty;

        // When
        Stream<String> interruptStream = StreamsUtils.interrupt(strings, interruptor);
        long count = interruptStream.count();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void should_interrupt_a_stream_correctly() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three", "", "", "", "", "");
        Predicate<String> interruptor = String::isEmpty;

        // When
        Stream<String> interruptStream = StreamsUtils.interrupt(strings, interruptor);
        List<String> list = interruptStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(3);
        assertThat(list).containsExactly("one", "two", "three");
    }

    @Test
    public void should_interrupt_a_sorted_stream_correctly_and_in_a_sorted_stream() {
        // Given
        SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList("one", "two", "three", "", "", "", "", ""));
        Predicate<String> interruptor = String::isEmpty;

        // When
        Stream<String> stream = StreamsUtils.interrupt(sortedSet.stream(), interruptor);

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(Spliterator.SORTED);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three", "", "", "", "", "");
        Predicate<String> interruptor = String::isEmpty;

        Stream<String> testedStream = StreamsUtils.interrupt(strings, interruptor);
        TryAdvanceCheckingSpliterator<String> spliterator = new TryAdvanceCheckingSpliterator<>(testedStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    public void should_correctly_call_the_onClose_callbacks_of_the_underlying_streams() {
        // Given
        AtomicBoolean b = new AtomicBoolean(false);
        Stream<String> strings = Stream.of("one", "two", "three", "", "", "", "", "").onClose(() -> b.set(true));
        Predicate<String> interruptor = String::isEmpty;

        // When
        StreamsUtils.interrupt(strings, interruptor).close();

        // Then
        assertThat(b.get()).isEqualTo(true);
    }

    @Test
    public void should_correctly_count_the_elements_of_a_sized_stream() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three", "", "", "", "", "");
        Predicate<String> interruptor = String::isEmpty;
        Stream<String> stream = StreamsUtils.interrupt(strings, interruptor);

        // When
        long count = stream.count();

        // Then
        assertThat(count).isEqualTo(3L);
    }
}