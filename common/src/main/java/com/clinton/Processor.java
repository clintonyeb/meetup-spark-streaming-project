package com.clinton;

@FunctionalInterface
public interface Processor<K, V> {
    void process(K key, V value);
}
