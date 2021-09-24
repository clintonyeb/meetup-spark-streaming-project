package com.clinton;

import java.util.function.Consumer;

public interface APIFetcher<K> {
    void fetch(Consumer<K> consumer);
}
