package cn.freeprogramming.config;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * 注解缓存的key
 *
 * @author humorchen
 * @date 2021/12/8 22:46
 */
@Configuration
public class AnnotationCacheKeyGenerator {
    /**
     * 分隔符
     */
    private static final String SEPARATOR = ":";

    @Bean
    public KeyGenerator myAnnotationCacheKeyGenerator() {
        return (o, method, objects) -> {
            StringBuilder builder = new StringBuilder();
            builder.append(o.getClass().getName());
            builder.append(SEPARATOR);
            builder.append(method.getName());
            if (objects.length > 0) {
                //只序列化第一个加入进去
                builder.append(SEPARATOR);
                builder.append(JSONObject.toJSONString(objects[0]));
            }
            String key = builder.toString();
            return key;
        };
    }
}
