package uk.co.optimisticpanda.serializedlambdaequality;

import java.io.Serializable;

@FunctionalInterface
public interface TestCollection<T> extends Serializable {

    T create(int maxResults);

    static TestCollection<String> getById(String id) {
        return maxResults -> id + "!";
    }

    static TestCollection<String> getByType(String type) {
        return maxResults -> type + "type";
    }

    static TestCollection<String> getAll() {
        return maxResults -> "all";
    }

    static TestCollection<String> getById(String id, int sort) {
        return maxResults -> id + "!" + sort;
    }

    static TestCollection<String> getByType(String type, int sort) {
        return maxResults -> type+ "!type" + sort;
    }

    static String all(int maxResults) {
        return "my string";
    }
}
