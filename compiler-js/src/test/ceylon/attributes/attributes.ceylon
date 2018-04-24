import check {...}

String firstName = "Gavin";

String lastName {
    return "King";
}

variable Integer flag = 0;
assign lastName {
    //print(lastName);
    flag = 1;
}

shared late variable Test6163 testObj;

shared class Test6163() {
    shared String someStringProperty = "";
}

shared void test() {
    checkEqual(lastName, "King", "toplevel getter");
    lastName = "Duke";
    checkEqual(flag, 1, "toplevel setter");
    
    Integer x { return 5; }
    assign x { flag = 2; }
    checkEqual(x, 5, "local getter");
    x = 7;
    checkEqual(flag, 2, "local setter");

    testNewSyntax();
    check(forwardAttributeTest.first == 1, "forwardAttributeTest");
    testObj = Test6163();
    check(testObj.someStringProperty == "", "#6163");
    testLazies();
    results();
}
{Integer+} forwardAttributeTest = { fat1, fat2, fat3 };
Integer fat1 = 1;
Integer fat2 { return 2; }
Integer fat3 => 3;
