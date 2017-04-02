Closure Equality
------------------

This is a small utility class to allow comparing java 8 closures.

There are quite a few limitations on java 8 lambdas/method references that makes comparing instances difficult.
This can cause issues when writing tests with a mocking framework.

```java
    String value = "world";
    Supplier<String> supplier -> () -> "Hello " + value; 
  
    service.doThingWithSupplier(supplier);

    // ... then in tests, the following will always fail:

    verify(service).doThingWithSupplier(supplier);
    
```

If we make a functional interface serializable then we can use reflection to access information about its declaration.
The information present in a serialized lambda also includes all closed arguments. 

The following test shows what can and cannot be achieved via this mechanism:

```java 
@Test
    public void closuresWithSameStateAreEqual() {
        test(TestCollection.getAll(), TestCollection.getAll()).areEqual();   
        test(TestCollection.getById("a"), TestCollection.getById("a")).areEqual();   
    }
    
    @Test
    public void sameMethodReferencesAreEqual() {
        TestCollection<String> collection = TestCollection::all;
        test(collection, collection).areEqual();   
    } 

    //http://stackoverflow.com/questions/28190304/two-exact-method-references-are-not-equal
    @Test
    public void duplicateMethodReferencesAreNotEqual() {
        TestCollection<String> collection1 = TestCollection::all;
        TestCollection<String> collection2 = TestCollection::all;
        
        test(collection1, collection2)
            .areDifferentBecause(
                "Impl methods do not match: [lambda$1] != [lambda$2]");   
    } 

    @Test
    public void closuresWithDifferentClosedArgsAreNotEqual() {
        test(TestCollection.getById("a"), TestCollection.getById("b"))
            .areDifferentBecause(
                "Lambda args do not match: [a] != [b]");   
    } 

    @Test
    public void closuresThatAreCreatedByDifferentMethodsDoNotMatch() {
        test(TestCollection.getById("a"), TestCollection.getByType("a"))
            .areDifferentBecause(
                    "Impl methods do not match: [lambda$0] != [lambda$1]");   
    } 

    @Test
    public void closuresWithSameSignatureButFromDifferentClassesAreDifferent() {
        test(TestCollection.getById("a"), DifferentTestCollection.getByType("b"))
            .areDifferentBecause(
                    "Capturing classes do not match: [uk/co/optimisticpanda/serializedlambdaequality/TestCollection] != [uk/co/optimisticpanda/serializedlambdaequality/DifferentTestCollection]",
                    "Lambda args do not match: [a] != [b]",
                    "functional interface classes do not match: [uk/co/optimisticpanda/serializedlambdaequality/TestCollection] != [uk/co/optimisticpanda/serializedlambdaequality/DifferentTestCollection]",
                    "Impl classes do not match: [uk/co/optimisticpanda/serializedlambdaequality/TestCollection] != [uk/co/optimisticpanda/serializedlambdaequality/DifferentTestCollection]");   
    } 

    @Test
    public void similarAnonymousClosuresAreNotEqual() {
        test((TestCollection<String>) i -> "", (TestCollection<String>) i -> "")
            .areDifferentBecause(
                    "Impl methods do not match: [lambda$3] != [lambda$4]");   
    } 

    @Test
    public void sameReferenceToSameAnonymousClosuresAreEqual() {
        TestCollection<String> collection = i -> "";
        test(collection, collection).areEqual();   
    } 
```

This is fine when you have control of the interface class as you can easily make it extend serializable. For other functions where you don't have
access to the source code this approach is still possible but requires taking advantage of [this java 8 feature](http://stackoverflow.com/a/22808112/1089998) that allows you to cast the object to a intersection of multiple types, as shown below:

```java
    @Test
    public void standardFunctionsCanWorkIfAssignedToIntersectionType() {
        Consumer<String> collection = (Consumer<String> & Serializable) string -> {
        };
        test((Consumer<String> & Serializable)collection, (Consumer<String> & Serializable)collection).areEqual();   
    }

    @Test
    public void serializableStandardFunctionsAreDifferentWithDifferentClosures() {
        Consumer<String> collection1 = (Consumer<String> & Serializable) string -> {
        };
        String a = "hello";
        Consumer<String> collection2 = (Consumer<String> & Serializable) string -> {
            System.out.println(a);
        };
        test((Consumer<String> & Serializable)collection1, (Consumer<String> & Serializable)collection2)
            .areDifferentBecause(
                    "Impl method signatures do not match: [(Ljava/lang/String;)V] != [(Ljava/lang/String;Ljava/lang/String;)V]",
                    "Lambda args do not match: [] != [hello]",
                    "Impl methods do not match: [lambda$7] != [lambda$8]");   
    }
```


This util class could be used in an argument matcher to verify that a specific closure was passed in.   


