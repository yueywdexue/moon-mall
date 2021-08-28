package com.yueyedexue.gulimall.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * @description:
 * 因为CacheProperties只使用了@ConfigurationProperties(prefix = "spring.cache")注解
 * 没有将其加入容器中, 所以我们自己的配置类中要想导入配置文件中设置的值,
 * 需要使用@EnableConfigurationProperties(CacheProperties.class)和这个类建立关联,
 * 好像这个注解也没将CacheProperties加入容器中
 * @author: MoonNightSnow
 * @createTime: 2021/8/11 15:14
 **/
@EnableConfigurationProperties(CacheProperties.class) // 与缓存配置文件建立关联
@Configuration
@EnableCaching //开启缓存
public class MyCacheConfiguration {

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        // 获得默认的redis配置类, 并在此基础上进行修改
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // 将缓存的value的存储方式设置成json格式
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));
        // 导入配置文件中的配置
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        // 导入过期时间
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        // 导入key前缀
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        // 导入缓存是否空值 放置缓存穿透
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        // 是否使用key前缀
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
