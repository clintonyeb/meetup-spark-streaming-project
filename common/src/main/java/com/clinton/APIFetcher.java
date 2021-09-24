package com.clinton;

import java.util.function.Consumer;
import java.util.function.Function;

public interface APIFetcher<K> {
    void fetch(Consumer<K> consumer);
}
