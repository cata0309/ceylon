"A [[Range]] of adjacent [[Enumerable]] values generated by 
 a [[first]] element, and a strictly positive [[size]]. The 
 range includes all values whose offset from `first` is 
 non-negative and less than the `size`."
see (`class Span`,
    `interface Enumerable`)
final serializable
class Measure<Element>(first, size)
        extends Range<Element>()
        given Element satisfies Enumerable<Element> {
    
    "The start of the range."
    shared actual Element first;
    
    "The size of the range."
    shared actual Integer size;
    
    "must be nonempty"
    assert (size > 0);
    
    string => first.string + ":" + size.string;
    
    last => first.neighbour(size - 1);
    
    longerThan(Integer length) => size > length;
    
    shorterThan(Integer length) => size < length;
    
    lastIndex => size - 1;
    
    rest => size > 1 
            then Measure(first.successor, size - 1)
            else [];
    
    getFromFirst(Integer index) 
            => index >= 0 && index < size
            then first.neighbour(index)
            else null;
    
    increasing => true;
    decreasing => false;
    
    iterator()
            => object satisfies Iterator<Element> {
        variable value count = 0;
        variable value current = first;
        shared actual Element|Finished next() {
            if (count >= size) {
                return finished;
            }
            else if (count++ == 0) {
                return current;
            }
            else {
                return ++current;
            }
        }
        string => "(``outer.string``).iterator()";
    };
    
    shared actual 
    {Element+} by(Integer step) {
        "step size must be greater than zero"
        assert (step > 0);
        return step == 1 then this else By(step);
    }
    
    class By(Integer step)
            satisfies {Element+} {
        
        size => 1 + (outer.size - 1) / step;
        
        first => outer.first;
        
        string => "(``outer``).by(``step``)";
        
        iterator() => object
                satisfies Iterator<Element> {
            variable value count = 0;
            variable value current = first;
            shared actual Element|Finished next() {
                if (count >= size) {
                    return finished;
                }
                else if (count++ == 0) {
                    return current;
                }
                else {
                    current = current.neighbour(step);
                    return current;
                }
            }
            string => "``outer``.iterator()";
        };
    }
    
    shifted(Integer shift) 
            => shift == 0 
            then this 
            else Measure(first.neighbour(shift), size);
    
    containsElement(Element x)
            => 0 <= x.offset(first) < size;
    
    shared actual 
    Boolean includesRange(Range<Element> range) {
        switch (range)
        case (is Measure<Element>) {
            value offset = range.first.offset(first);
            return 0 <= offset <= size - range.size;
        }
        case (is Span<Element>) {
            if (range.decreasing) {
                return false;
            } else {
                value offset = range.first.offset(first);
                return 0 <= offset <= size - range.size;
            }
        }
    }
    
    shared actual 
    Boolean equals(Object that) {
        if (is Measure<out Object> that) {
            //optimize for another Measure
            return that.size == size && that.first == first;
        } else if (is Span<out Object> that) {
            return that.increasing &&
                    that.first == first && that.size == size;
        } else {
            //it might be another sort of List
            return super.equals(that);
        }
    }
    
    shared actual 
    Element[] measure(Integer from, Integer length) {
        if (length <= 0) {
            return [];
        } else {
            value len = from + length < size 
                    then length
                    else size - from;
            return Measure(first.neighbour(from), len);
        }
    }
    
    shared actual 
    Element[] span(Integer from, Integer to) {
        if (from <= to) {
            if (to < 0 || from >= size) {
                return [];
            } else {
                value len = to < size 
                        then to - from + 1
                        else size - from;
                return Measure(first.neighbour(from), len);
            }
        } else {
            if (from < 0 || to >= size) {
                return [];
            } else {
                value len = from < size 
                        then from - to + 1
                        else size - to;
                return Measure(first.neighbour(to), len).reversed;
            }
        }
    }
    
    shared actual 
    Element[] spanFrom(Integer from) {
        if (from <= 0) {
            return this;
        } else if (from < size) {
            return Measure(first.neighbour(from), size - from);
        } else {
            return [];
        }
    }
    
    shared actual 
    Element[] spanTo(Integer to) {
        if (to < 0) {
            return [];
        } else if (to < size - 1) {
            return Measure(first, to);
        } else {
            return this;
        }
    }
    
    shared actual void each(void step(Element element)) {
        variable value current = first;
        variable value count = 0;
        while (count++<size) {
            step(current++);
        }
    }
}

"Produces a [[Range]] of adjacent [[Enumerable]] values 
 generated by a [[first]] element, and a strictly positive 
 [[size]], or returns the [[empty sequence|empty]] if 
 `size <= 0`. The range includes all values whose offset 
 from `first` is non-negative and less than the `size`.
 
 More precisely, if `x` and `first` are of `Enumerable` 
 type `X`, and `size` is an integer, then `x in first:size` 
 if and only if `0 <= x.offset(first) < size`.
 
 The _measure operator_ `:` is an abbreviation for
 `measure()`:
 
     for (i in start:size) { ... }
     for (char in '0':10) { ... }
 
 The measure operator accepts the first index and size of 
 the range:
 
     0:5     // [0, 1, 2, 3, 4]
 
 If the size is nonpositive, the range is empty:
 
     0:0     // []
     5:0     // []
     0:-5    // []"
since("1.1.0")
shared Range<Element>|[] measure<Element>
        (Element first, Integer size)
        given Element satisfies Enumerable<Element>
        => size <= 0 then [] else Measure(first, size);
