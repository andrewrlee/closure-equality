package uk.co.optimisticpanda.serializedlambdaequality;

import java.io.Serializable;

@FunctionalInterface
public interface DifferentTestCollection<T> extends Serializable {

    T create(int maxResults);

    static DifferentTestCollection<String> getByType(String type) {
        return maxResults -> type + "type";
    }
}
