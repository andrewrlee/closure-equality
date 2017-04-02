package uk.co.optimisticpanda.serializedlambdaequality;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.optimisticpanda.serializedlambdaequality.ClosureEqualityResult.check;

import java.io.Serializable;
import java.util.HashSet;
import java.util.function.Consumer;

import org.junit.Test;

public class ClosureEqualityResultTest {

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
    
    private Check test(Serializable s1, Serializable s2) {
        return new Check(s1, s2);
    }
    
    private static class Check {
        
        private ClosureEqualityResult result;

        private Check(Serializable s1, Serializable s2) {
            this.result = check(s1, s2);
        }
        
        private Check areEqual() {
            assertThat(result.isEqual()).describedAs("Should be equal").isTrue();
            return this;
        }
        
        private Check areDifferentBecause(String... errors) {
            assertThat(result.isEqual()).describedAs("Should be different").isFalse();
            assertThat(result.getDifferences()).describedAs("differences should contain").isEqualTo(new HashSet<>(asList(errors)));
            return this;
        }
    }
}
