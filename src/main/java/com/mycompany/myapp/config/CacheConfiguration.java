package com.mycompany.myapp.config;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.hibernate.cache.jcache.ConfigSettings;

import java.io.Serializable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import io.github.jhipster.config.JHipsterProperties;

@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    @Autowired(required = false)
    GitProperties gitProperties;

    @Autowired
    BuildProperties buildProperties;

    @Bean
    public KeyGenerator keyGenerator() {
        String shortCommitId = gitProperties.getShortCommitId();
        Instant time = buildProperties.getTime();
        String version = buildProperties.getVersion();
        Object prefixObject = ObjectUtils.firstNonNull(shortCommitId, time, version);

        String prefix = null;
        if (prefixObject instanceof  Instant) {
            prefix = DateTimeFormatter.ISO_INSTANT.format((Instant) prefixObject);
        } else {
            prefix = prefixObject.toString();
        }
        System.out.println(prefix);
        return new JHipsterKeyGenerator(gitProperties.getShortCommitId());
    }

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(JHipsterProperties jHipsterProperties) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();
        Config config = new Config();
        config.useSingleServer().setAddress(jHipsterProperties.getCache().getRedis().getServer());
        jcacheConfig.setStatisticsEnabled(true);
        jcacheConfig.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, jHipsterProperties.getCache().getRedis().getExpiration())));
        return RedissonConfiguration.fromInstance(Redisson.create(config), jcacheConfig);
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cm) {

        Map<String, Object> hibernateProperties = new HashMap<>();
        hibernateProperties .put("hibernate.cache.region_prefix ", gitProperties.getShortCommitId());
        hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cm);

        return hibernateProperties1 -> hibernateProperties1.putAll(hibernateProperties);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        return cm -> {
            createCache(cm, com.mycompany.myapp.repository.UserRepository.USERS_BY_LOGIN_CACHE, jcacheConfiguration);
            createCache(cm, com.mycompany.myapp.repository.UserRepository.USERS_BY_EMAIL_CACHE, jcacheConfiguration);
            createCache(cm, com.mycompany.myapp.domain.User.class.getName(), jcacheConfiguration);
            createCache(cm, com.mycompany.myapp.domain.Authority.class.getName(), jcacheConfiguration);
            createCache(cm, com.mycompany.myapp.domain.User.class.getName() + ".authorities", jcacheConfiguration);
            // jhipster-needle-redis-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName, javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cm.destroyCache(cacheName);
        }
        cm.createCache(cacheName, jcacheConfiguration);
    }

}
