package uk.co.optimisticpanda.serializedlambdaequality;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ClosureEqualityResult {

    private List<String> differences = new ArrayList<>();

    public static ClosureEqualityResult check(Serializable serializable1, Serializable serializable2) {
        SerializedLambda lambda1 = toSerializedLambda(serializable1);
        SerializedLambda lambda2 = toSerializedLambda(serializable2);
        
        return new ClosureEqualityResult()
                    .add(lambda1, lambda2, ClosureEqualityResult::getArgs, "Lambda args do not match: %s != %s")
                    .add(lambda1, lambda2, SerializedLambda::getCapturingClass, "Capturing classes do not match: [%s] != [%s]")
                    .add(lambda1, lambda2, SerializedLambda::getImplClass, "Impl classes do not match: [%s] != [%s]")
                    .add(lambda1, lambda2, SerializedLambda::getImplMethodName, "Impl methods do not match: [%s] != [%s]")
                    .add(lambda1, lambda2, SerializedLambda::getImplMethodSignature, "Impl method signatures do not match: [%s] != [%s]")
                    .add(lambda1, lambda2, SerializedLambda::getFunctionalInterfaceClass, "functional interface classes do not match: [%s] != [%s]")
                    .add(lambda1, lambda2, SerializedLambda::getFunctionalInterfaceMethodName, "functional interface method names do not match: [%s] != [%s]")
                    .add(lambda1, lambda2, SerializedLambda::getFunctionalInterfaceMethodSignature, "functional interface method signatures do not match: [%s] != [%s]")                    ;
    }

    private static SerializedLambda toSerializedLambda(Serializable serializable1) {
        try {
            Method replaceMethod = serializable1.getClass().getDeclaredMethod("writeReplace");
            replaceMethod.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) replaceMethod.invoke(serializable1);
            return lambda;
        } catch (Exception e) {
            throw new RuntimeException("Problem creating serialized lambda", e);
        }
        
    }

    private <T, R> ClosureEqualityResult add(T t1, T t2, Function<T, R> f, String message) {
        R r1 = f.apply(t1);
        R r2 = f.apply(t2);
        
        if (!r1.equals(r2)) {
            differences.add(String.format(message, r1, r2));
        }
        return this;
    }
    
    public Set<String> getDifferences() {
        return new HashSet<>(differences);
    }
    
    public boolean isEqual() {
        return differences.isEmpty();
    }

    private static List<Object> getArgs(SerializedLambda lambda) {
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < lambda.getCapturedArgCount(); i++) {
            args.add(lambda.getCapturedArg(i));
        }
        return args;
    }    
}
