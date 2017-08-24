"Given a [[stream|Iterable]] of streams, return a 
 transposed stream of streams, where the nth element 
 of the transposed stream is a stream comprising the 
 nth elements of the given streams, padded with `null`s. 
 For example,
 
     transpose({\"hello\", \"world\", 1..3})
 
 produces the stream 
 `{ { 'h', 'w', 1 }, { 'e', 'o', 2 }, { 'l', 'r', 3 }, 
  { 'l', 'l', null }, { 'o', 'd', null } }`."
shared {{Element?+}*} transpose<Element,Absent>
        ("The streams to be transposed."
         Iterable<{Element*},Absent> streams)
        given Absent satisfies Null
        => object satisfies {{Element?+}*} {
    value iterators 
            = streams.collect((stream) => stream.iterator());
    class Done { shared new done {} }
    iterator() => object satisfies Iterator<{Element?+}> {
        shared actual {Element?+}|Finished next() {
            if (nonempty iterators) {
                value elements 
                        = iterators.collect((iter)
                            => if (!is Finished next = iter.next())
                               then next else Done.done);
                return elements.any((it) => !it is Done)
                then elements.map((element)
                    => if (is Done element)
                       then null else element)
                else finished;
            }
            else {
                return finished;
            }
        }
    };
};
