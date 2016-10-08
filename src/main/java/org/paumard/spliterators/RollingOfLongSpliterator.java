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

import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.LongStream;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class RollingOfLongSpliterator implements Spliterator<LongStream> {

	private final int grouping ;
	private final OfLong spliterator ;
	private final long [] buffer ;
	private final AtomicInteger bufferWriteIndex = new AtomicInteger(0) ;
	private final AtomicInteger bufferReadIndex = new AtomicInteger(0) ;

	public static RollingOfLongSpliterator of(OfLong spliterator, int grouping) {
		Objects.requireNonNull(spliterator);
		if (grouping < 2) {
            throw new IllegalArgumentException ("Why would you be creating a rolling spliterator with a grouping factor of less than 2?");
        }
        if ((spliterator.characteristics() & Spliterator.ORDERED) == 0) {
            throw new IllegalArgumentException ("Why would you create a rolling spliterator on a non-ordered spliterator?");
        }

		return new RollingOfLongSpliterator(spliterator, grouping);
	}

	private RollingOfLongSpliterator(OfLong spliterator, int grouping) {
		this.spliterator = spliterator ;
		this.grouping = grouping ;
		this.buffer = new long[grouping + 1] ;
	}


	@Override
	public boolean tryAdvance(Consumer<? super LongStream> action) {
		boolean finished = false ;

		if (bufferWriteIndex.get() == bufferReadIndex.get()) {
			for (int i = 0 ; i < grouping ; i++) {
				if (!advanceSpliterator()) {
					finished = true ;
				}
			}
		}
		if (!advanceSpliterator()) {
			finished = true ;
		}

		LongStream subStream = buildSubstream() ;
		action.accept(subStream) ;
		return !finished ;
	}

	private boolean advanceSpliterator() {
		return spliterator.tryAdvance(
				(long element) -> {
					buffer[bufferWriteIndex.get() % buffer.length] = element ;
					bufferWriteIndex.incrementAndGet() ;
				});
	}

	@Override
	public Spliterator<LongStream> trySplit() {
		OfLong splitSpliterator = spliterator.trySplit();
		return splitSpliterator == null ? null : new RollingOfLongSpliterator(splitSpliterator, grouping) ;
	}

	private LongStream buildSubstream() {

		LongStream.Builder subBuilder = LongStream.builder() ;
		for (int i = 0 ; i < grouping ; i++) {
			subBuilder.add(buffer[(i + bufferReadIndex.get()) % buffer.length]) ;
		}
		bufferReadIndex.incrementAndGet() ;
		return subBuilder.build() ;
	}

	@Override
	public long estimateSize() {
		long estimateSize = spliterator.estimateSize();
		return estimateSize == Long.MAX_VALUE ? Long.MAX_VALUE : estimateSize - grouping;
	}

	@Override
	public int characteristics() {
        // this spliterator is already ordered
		return spliterator.characteristics();
	}
}