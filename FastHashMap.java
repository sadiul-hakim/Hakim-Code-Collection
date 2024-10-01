package org.javase;

import java.util.LinkedList;
import java.util.StringJoiner;

public class FastHashMap<K, V> {
    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;
    private int size;
    private LinkedList<Entity<K, V>>[] buckets;

    public FastHashMap(){
        this.buckets = new LinkedList[INITIAL_CAPACITY];
    }

    private static class Entity<K, V> {
        K key;
        V value;

        Entity(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public void put(K key, V value) {
        int index = getKeyIndex(key);
        var bucket = buckets[index];

        if (bucket == null) {
            bucket = new LinkedList<>();
            buckets[index] = bucket;
        }

        for (var entity : bucket) {
            if ((key == null && entity.key == null) || (key != null && entity.key.equals(key))) {
                entity.value = value;
                return;
            }
        }

        bucket.add(new Entity<>(key, value));
        size++;

        if (size > buckets.length * LOAD_FACTOR) {
            resize();
        }
    }

    public V get(K key) {
        var index = getKeyIndex(key);
        var bucket = buckets[index];

        if (bucket == null)
            return null;

        for (var entity : bucket) {
            if ((key == null && entity.key == null) || (key != null && entity.key.equals(key))) {
                return entity.value;
            }
        }

        return null;
    }

    public void remove(K key) {
        var index = getKeyIndex(key);
        var bucket = buckets[index];

        if (bucket == null)
            return;

        for (var entity : bucket) {
            if ((key == null && entity.key == null) || (key != null && entity.key.equals(key))) {
                bucket.remove(entity);
                size--;
                return;
            }
        }
    }


    public void resize() {
        var oldBuckets = buckets;
        buckets = new LinkedList[buckets.length * 2];
        size = 0;

        for (var oldBucket : oldBuckets) {
            if (oldBucket != null) {
                for (var bucket : oldBucket) {
                    put(bucket.key, bucket.value);
                }
            }
        }
    }

    private int getKeyIndex(K key) {
        int hashCode = key == null ? 0 : key.hashCode();
        return Math.abs(hashCode) % buckets.length;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        buckets = new LinkedList[INITIAL_CAPACITY];
        size = 0;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public void print() {
        if (isEmpty()) {
            return;
        }

        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (var bucket : buckets) {
            if (bucket == null)
                continue;

            for (var entity : bucket) {
                joiner.add(entity.key + "=" + entity.value);
            }
        }
        System.out.println(joiner);
    }
}
