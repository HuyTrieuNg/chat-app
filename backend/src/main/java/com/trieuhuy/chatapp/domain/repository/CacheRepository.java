package com.trieuhuy.chatapp.domain.repository;

import java.time.Duration;
import java.util.Optional;

public interface CacheRepository {
    
    <T> void set(String key, T value, Duration ttl);
    
    <T> Optional<T> get(String key, Class<T> type);
    
    void evict(String key);
    
    void evictPattern(String pattern);
}
