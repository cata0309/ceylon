"The classic Hello World program"
shared void hello(String name = "World") {
    print("Hello, `` name ``!");
}

"The runnable method of the module." 
shared void run(){
    if (nonempty args=process.arguments) {
        for (arg in args) {
            hello(arg);
        }
    }
    else {
        hello();
    }
}
