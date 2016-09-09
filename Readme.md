# Streams Utils

In the Java 8 Stream API, a classical stream operation can only work on the current element of the given stream, but cannot take into account the previous elements.

The right solution is to change the stream itself. Suppose we have a stream `{a, b, c, d, ...}` and that we need to remember the value of `a` when we process `b`. The problem can be solved by changing this stream to the following : `{[a, b], [b, c], [c, d], ...}`. We go from a stream of objects to a stream of pairs of objects, or maybe a stream of streams of objects.

This problem can be solved by creating a spliterator on the original stream spliterator. This is the object of this API.

The entry point of this API is meant to be the `StreamsUtils` factory class. Reading the Javadoc is a good idea, patterns are provided.

You can use this API directly with Maven, by adding the following dependency.  

```
<dependency>
    <groupId>org.paumard</groupId>
    <artifactId>streams-utils</artifactId>
    <version>1.0</version>
</dependency>
```

This project was previously called More Spliterators (you can see it on my more-spliterators Github repo). Quite oddly, nobody had any clue about what is was about. So I decided to rename it to a more explicit name. 

# Operations provided

## Cycling

Takes a stream and repeats it forever, as long as this stream has a finite size. 

## Grouping

Takes a stream `[a, b, c, d]` and returns `[[a, b], [c, d]]`. The grouping factor is parametrized.

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
Stream<Class<?>> interruptedStreamOfClasses = MoreSpliterator.interrupting(streamOfClasses, Objects::isNull);
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

## Acknowledgements

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