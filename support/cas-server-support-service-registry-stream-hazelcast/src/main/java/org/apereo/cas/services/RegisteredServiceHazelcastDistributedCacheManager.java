package org.apereo.cas.services;

import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServiceHazelcastDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class RegisteredServiceHazelcastDistributedCacheManager extends
    BaseDistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> {

    private final HazelcastInstance instance;

    private final IMap<String, DistributedCacheObject<RegisteredService>> mapInstance;

    @Override
    public void close() {
        this.instance.shutdown();
    }

    @Override
    public Collection<DistributedCacheObject<RegisteredService>> getAll() {
        return this.mapInstance.values();
    }

    @Override
    public DistributedCacheObject<RegisteredService> get(final RegisteredService service) {
        if (contains(service)) {
            val key = buildKey(service);
            return this.mapInstance.get(key);
        }
        return null;
    }

    @Override
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> set(final RegisteredService key,
                                                                                                     final DistributedCacheObject<RegisteredService> item) {
        LOGGER.debug("Broadcasting service definition [{}] via Hazelcast...", item);
        this.mapInstance.set(buildKey(key), item);
        return this;
    }

    @Override
    public boolean contains(final RegisteredService service) {
        val key = buildKey(service);
        return this.mapInstance.containsKey(key);
    }

    @Override
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> remove(final RegisteredService service,
                                                                                                        final DistributedCacheObject<RegisteredService> item) {
        val key = buildKey(service);
        this.mapInstance.remove(key);
        return this;
    }

    @Override
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> update(final RegisteredService service,
                                                                                                        final DistributedCacheObject<RegisteredService> item) {
        remove(service, item);
        set(service, item);
        return this;
    }

    @Override
    public Collection<DistributedCacheObject<RegisteredService>> findAll(
        final Predicate<DistributedCacheObject<RegisteredService>> filter) {
        return getAll().stream().filter(filter).collect(Collectors.toList());
    }
}
