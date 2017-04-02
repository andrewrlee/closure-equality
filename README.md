###Closure Equality

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

This util class could be used in an argument matcher to verify that a specific closure was passed in.   


