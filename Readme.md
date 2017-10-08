# Streams Utils

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.paumard/streams-utils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.paumard/streams-utils) [![Build Status](https://travis-ci.org/JosePaumard/streams-utils.png?branch=master)](https://travis-ci.org/JosePaumard/streams-utils) [![Coverage Status](https://coveralls.io/repos/JosePaumard/streams-utils/badge.svg?branch=master&service=github)](https://coveralls.io/github/JosePaumard/streams-utils?branch=master) [![Javadocs](http://javadoc.io/badge/org.paumard/streams-utils.svg)](http://javadoc.io/doc/org.paumard/streams-utils)


In the Java 8 Stream API, a classical stream operation can only work on the current element of the given stream, but cannot take into account the previous elements.

The right solution is to change the stream itself. Suppose we have a stream `{a, b, c, d, ...}` and that we need to remember the value of `a` when we process `b`. The problem can be solved by changing this stream to the following : `{[a, b], [b, c], [c, d], ...}`. We go from a stream of objects to a stream of pairs of objects, or maybe a stream of streams of objects.

This problem can be solved by creating a spliterator on the original stream spliterator. This is the object of this API.

The entry point of this API is meant to be the `StreamsUtils` factory class. Reading the Javadoc is a good idea, patterns are provided.

You can use this API directly with Maven, by adding the following dependency.  

```
<dependency>
    <groupId>org.paumard</groupId>
    <artifactId>streams-utils</artifactId>
    <version>1.7</version>
</dependency>
```

This project was previously called More Spliterators (you can see it on my more-spliterators Github repo). Quite oddly, nobody had any clue about what is was about. So I decided to rename it to a more explicit name. 

## Acknowledgments

I had the chance to present part of this project at JavaOne 2017. By the way you can watch it on [YouTube](http://www.youtube.com/watch?v=xgHGpsubL5M) and grab the slides on [Slideshare](http://www.slideshare.net/jpaumard/streams-in-the-wild), 
Doctor Deprecator ([@DrDeprecator](https://www.twitter.com/DrDeprecator) aka Stuart Marks [@StuartMarks](https://twitter.com/StuartMarks)) was kind enough to come to my talk, and it was a great chance because he could spot a sneaky mistake I made both in the talk and in this API. 

It turns out that there is a specification that states the following about the `tryAdvance(Consumer<T>)` method of the `Spliterator` interface: 
- if this method returns true then the consumer passed as a parameter should be called once and only once;
- if this method returns false, then this consumer should not be called. 

Not conforming to this point may lead to unspecified behavior, including silent loss of elements in the produced stream. Even it you did not see this happening, it may in the future, including with future versions
of the Stream API.   

This bug was indeed there in Streams Utils, and is fixed in the 1.7 version. 

Thank you Stuart for your most valuable comments and advice!  

# Operations provided

## Cycling

Takes a stream and repeats it forever, as long as this stream has a finite size. 

## Grouping

Takes a stream `[a, b, c, d]` and returns `[[a, b], [c, d]]`. The grouping factor is parametrized.

## Grouping on gating

Takes a stream `[X, a, b, Y, c, X, d, Y]` and returns `[[X, a, b, Y], [X, d, Y]]`. The grouping takes place when a certain predicate is met, both for the opening and the closing operation. This grouping operation is controlled by two provided predicates. The opening and closing elements can be added to the substreams or not.    

## Repeating

Takes a stream `[a, b, c, d]` and returns `[a, a, b, b, c, c, d, d]`. The repeating factor is parametrized.

## Rolling

Takes a stream `[a, b, c, d, e]` and returns `[[a, b], [b, c], [c, d], [d, e]]`. The rolling factor is parametrized.

## Traversing

Takes a set of streams and builds a stream of substreams. Each substream is made of the nth element of the corresponding stream. For instance, if we have:
```
stream0 = ["a00", "a01", "a02", "a03"]
stream1 = ["a10", "a11", "a12", "a13"]
stream2 = ["a20", "a21", "a22", "a23"]
stream3 = ["a30", "a31", "a32", "a33"]
```

The resulting stream is the following:
```
[["a00", "a10", "a20", "a30"],
 ["a01", "a11", "a21", "a31"],
 ["a02", "a12", "a22", "a32"],
 ["a03", "a13", "a23", "a33"]]
```

## Validating

A validating spliterator works with a predicate and two mapping functions. When the predicate applied to the current element of the stream is true, then the first mapper is applied to that element. If it is false, then the second mapper is applied. In both cases the mapped element is added to the new spliterator.

This validating spliterator could also be implemented with a mapping function.

## Interrupting

The interrupting spliterator takes an interruptor as a parameter. If this interruptor became false for a given element of the provided stream, then it ends the returned stream.

It has been created to handle the following case. We created an iterating stream on a class hierarchy:

```
String<Class<?>> streamOfClasses = Stream.iterate(myClass, c -> c == null ? null : c.getSuperclass());
```

The problem is that, once the `Object.class` has been met, this stream is null. We wanted to stop it. This is exactly what the interrupting spliterator does.

```
Stream<Class<?>> interruptedStreamOfClasses = StreamsUtils.interrupting(streamOfClasses, Objects::isNull);
```

The returned stream in this case will generate elements up to `Object.class` and will stop. 


## Gating

The gating spliterator does the opposite of the interrupting spliterator. Here we provide a validator. The returned stream will transmit the elements of the input stream one the provided validator has seen a valid element.

## Weaving

The weaving operator is another version of the traversing operator, it could be seen as a traversing followed by a flatmap. Here is an example:
```
stream0 = ["a00", "a01", "a02", "a03"]
stream1 = ["a10", "a11", "a12", "a13"]
stream2 = ["a20", "a21", "a22", "a23"]
stream3 = ["a30", "a31", "a32", "a33"]
```

The resulting stream is the following:
```
["a00", "a10", "a20", "a30", "a01", "a11", "a21", "a31",
 "a02", "a12", "a22", "a32", "a03", "a13", "a23", "a33"]
```

## Zipping

The zipping operator takes two streams and a bifunction. The resulting stream is the application of the bifunction on two elements of the streams, one at a time. 

## Collection on a shifting window

This method takes a `Stream<E>` as a parameter, the size of the window and a collector. This collector is applied on each shifting window, computes values
that are returned in a stream. 

## Computation of averages on a shifting window
 
A set of methods computes such an average on a stream of objects, returning a `DoubleStream` with each value being the average on a given window. This
average can be computed on `int`, `long` and `double` using the corresponding standard methods. 

A first version of these methods takes a `Stream<E>` as a parameter, and a mapper to the corresponding primitive type. The computation first maps the stream before 
computing the average of the shifting window. 

A second version takes a stream of primitive types as a parameter (`IntStream`, `LongStream` or `DoubleStream`) and does the computation directly on it. 
The implementation is built in such a way as no boxing / unboxing is done. 

## Computation of a summary statistics on a shifting window
 
This set of methods works the same as for the computation of averages. The difference is that a summary statistics is computed (for `int`, `long` or `double`)
on each window. There are two versions of these methods: the first takes a `Stream<E>` and a mapper, the second directly a stream of primitive types. 

## Cross product operations

Three cross product operations have been provided. They all take a `Stream<E>` and return a `Stream<Map.Entry<E, E>>`. The
 resulting stream is the result of the Cartesian product of the provided stream with itself. 

This operation is available with three flavors. 

Vanilla: `StreamsUtils.crossProduct(stream)`. For `{a, b, c}`, it returns `{(a, a), (a, b), (a, c), (b, a), (b, b), (b, c), (c, a), (c, b), (c, c)}` where `(a, b)` is a `Map.Entry` of key `a` and value `b`.
 
With no doubles: `StreamsUtils.crossProductNoDoubles(stream)`. For `{a, b, c}`, it returns `{(a, b), (a, c), (b, a), (b, c), (c, a), (c, b)}`. No couples for wich `key.equals(value)` is true is produced. 
  
With no doubles and ordered couples: `StreamsUtils.crossProductOrdered(stream, comparator)`. For `{a, b, c}`, it returns `{(a, b), (a, c), (b, c)}`. In this stream, all the key / value pairs are such that `comparator.compare(a, b)` is lesser than 0. There is also a `StreamsUtils.crossProductNaturallyOrdered(stream)` that takes the `Comparator.naturalOrder()` comparator to compare `a` and `b`. 

Do not try on a non-finite stream...

## Grouping on splitting

This operator comes in two flavors. It builds a stream of streams using two predicates. 

The first one takes two predicates. If the first predicate matched a given element, then a gate is opened, and the elements of the input stream are accumulated in a first stream. Then the second predicate is used. If it matches a subsequent element, then the gate is closed and the substream is returned. The process then starts again until the elements of the input stream are exhausted. It is possible to choose whether to add the opening and closing elements to the substreams or not. 

The second one takes only one predicate, that is used both for opening and closing. 

## Filtering all maxes

This operator returns a filtered stream with only the greatest elements in it. It comes in two flavor: the first one uses the natural order comparator, and works with streams of comparable elements. The second one takes a comparator as a parameter. 

For the following stream: `"1", "2", "4", "1", "2", "3", "3", "4"` the filtered stream is `"4", "4"`. 

## Filtering N maxes

This operator takes a comparator and a number as parameters. This comparator can be the natural order comparator. It then comes in two flavors. 

The first one returns a least N elements with duplicates, ordered in the decreasing order, starting from the max. If there are duplicates in the input stream, then those duplicates are kept. It guarantees that all the elements of the same value are returned. 

Suppose we have the following stream: `"1", "2", "4", "1", "2", "3", "3", "4"` 
- If we ask for 2 max values, then we get `"4", "4"`. 
- If we ask for 3 max values, then we get `"4", "4", "3", "3"`, because the operator returns all the `"3"`. 

This first operator is called _max values_ because in the case the elements of the stream are entries, and the comparison is made by keys, we want to be sure to have all the values associated with a given key. 

The second flavor is called _max_keys_ because it removes the duplicates, and count the number of keys instead of values. Thus, in our previous example :
- If we ask for 2 max keys, then we get `"4", "3"`. 
- If we ask for 3 max keys, then we get `"4", "3", "2"`.
 
In this second case, we do not have duplicated elements (in the sense of the provided comparator) in the returned stream. If the number of different elements in the input stream is not enough, we may have less than N elements.  

## Accumulating

This operator accumulates the elements of a stream using a binary operator. Suppose we have the following stream: ```a0, a1, a2, a3```

The resulting stream, for an operator `op` is the following: ```a0, r1, r2, r3```
 
 where: 
 ```
    r1 = op(a0, a1)
    r2 = op(r1, a2)
    r3 = op(r2, a3)
```

This stream can also operate directly on map entries, accumulating the values of the entries. 

## Acknowledgments

Many thanks to Rémi Forax for his valuable advice during the development of this API. 

Now Rémi you can implement `FizzBuzz` using the following code:
```
Stream<String> fizzBuzz = 
   zip(
      IntStream.range(0, 101).boxed(), 
      zip(
         cycle(Stream.of("fizz", "", "")), 
         cycle(Stream.of("buzz, "", "", "", ""))
         String::concat
      ), 
      (i, string) -> string.isEmpty() ? i.toString() : string
   );
fizzBuzz.skip(1).forEach(System.out::println);
```