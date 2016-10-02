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
import java.util.stream.DoubleStream;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class RollingOfDoubleSpliterator implements Spliterator<DoubleStream> {

	private final int grouping ;
	private final OfDouble spliterator ;
	private double [] buffer ;
	private AtomicInteger bufferWriteIndex = new AtomicInteger(0) ;
	private AtomicInteger bufferReadIndex = new AtomicInteger(0) ;

	public static RollingOfDoubleSpliterator of(OfDouble spliterator, int grouping) {
		Objects.requireNonNull(spliterator);
		if (grouping < 2) {
            throw new IllegalArgumentException ("Why would you be creating a rolling spliterator with a grouping factor of less than 2?");
        }
        if ((spliterator.characteristics() & Spliterator.ORDERED) == 0) {
            throw new IllegalArgumentException ("Why would you create a rolling spliterator on a non-ordered spliterator?");
        }

		return new RollingOfDoubleSpliterator(spliterator, grouping);
	}

	private RollingOfDoubleSpliterator(OfDouble spliterator, int grouping) {
		this.spliterator = spliterator ;
		this.grouping = grouping ;
		this.buffer = new double[grouping + 1] ;
	}


	@Override
	public boolean tryAdvance(Consumer<? super DoubleStream> action) {
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

		DoubleStream subStream = buildSubstream() ;
		action.accept(subStream) ;
		return !finished ;
	}

	private boolean advanceSpliterator() {
		return spliterator.tryAdvance(
				(double element) -> {
					buffer[bufferWriteIndex.get() % buffer.length] = element ;
					bufferWriteIndex.incrementAndGet() ;
				});
	}

	@Override
	public Spliterator<DoubleStream> trySplit() {
		OfDouble splitSpliterator = spliterator.trySplit();
		return splitSpliterator == null ? null : new RollingOfDoubleSpliterator(splitSpliterator, grouping) ;
	}

	private DoubleStream buildSubstream() {

		DoubleStream.Builder subBuilder = DoubleStream.builder() ;
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