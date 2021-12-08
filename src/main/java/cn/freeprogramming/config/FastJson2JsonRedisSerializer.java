package cn.freeprogramming.config;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * fastjson序列化器
 *
 * @author humorchen
 * @date 2021/12/8 22:56
 */
@Component
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    static {
        //由于fastjson有autotype检查，将自己的包前缀加进去防止反序列化失败
        String packageName = FastJson2JsonRedisSerializer.class.getPackage().getName();
        String prefix = "com";
        int dotIndex = packageName.indexOf('.');
        if (dotIndex != -1){
            prefix = packageName.substring(0,dotIndex);
        }
        ParserConfig.getGlobalInstance().addAccept(prefix);
    }

    private Class<T> clazz;

    public FastJson2JsonRedisSerializer() {
        this((Class<T>) Object.class);
    }

    public FastJson2JsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }

        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);

        return (T) JSON.parseObject(str, clazz);
    }

}

