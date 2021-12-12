package cn.freeprogramming.dao;

import cn.freeprogramming.cache.ICacheKey;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis操作封装类
 * @author humorchen
 * @date 2021/12/11 22:01
 */
public class RedisDao {
    private static RedisTemplate redisTemplate;

    /**
     * 设置redisTemplate
     * @param template
     */
    public static void setRedisTemplate(RedisTemplate template){
        if (redisTemplate == null){
            redisTemplate = template;
        }else {
            throw new RuntimeException("RedisDao的RedisTemplate进允许设置一次");
        }
    }

    /**
     * 查询匹配的keys（慎用！！！）
     * @param pattern
     * @return
     */
    public static Set<String> keys(String pattern){
        return redisTemplate.keys(pattern);
    }

    /**
     * 查询以prefix开头的所有key
     * @param prefix
     * @return
     */
    public static Set<String> keysStartWith(String prefix){
        return keys(prefix+"*");
    }

    /**
     * 删除Key
     * @param cacheKey
     */
    public static void del(ICacheKey cacheKey){
        redisTemplate.delete(cacheKey.getKey());
    }

    /**
     * 删除一批
     * @param cacheKeys
     * @return
     */
    public static Long del(Collection<ICacheKey> cacheKeys){
        return redisTemplate.delete(cacheKeys.stream().map(cacheKey -> cacheKey.getKey()).collect(Collectors.toList()));
    }

    /**
     * 批量删除以prefix开头的所有键
     * @param prefix
     * @return
     */
    public static Long delStartWith(String prefix){
        Set<String> keys = keysStartWith(prefix);
        if (keys != null && keys.size() > 0){
            return redisTemplate.delete(keys);
        }
        return 0L;
    }

    /**
     * 对key自增1
     * @param cacheKey
     * @return
     */
    public static Long incr(ICacheKey cacheKey){
        return incr(cacheKey,1L);
    }

    /**
     * 对Key自增delta（正负都可以的），也可以设置过期时间
     * @param cacheKey
     * @param delta
     * @return
     */
    public static Long incr(ICacheKey cacheKey,Long delta){
        String key = cacheKey.getKey();
        Long ret = redisTemplate.opsForValue().increment(key,delta);
        if (cacheKey.getExpire() > 0){
            redisTemplate.expire(key,cacheKey.getExpire(),cacheKey.getExpireTimeUnit());
        }
        return ret;
    }

    /**
     * 主动修改过期时间
     * @param cacheKey
     * @param expire
     * @param timeUnit
     * @return
     */
    public static Boolean expire(ICacheKey cacheKey, Integer expire, TimeUnit timeUnit){
        return redisTemplate.expire(cacheKey.getKey(),expire,timeUnit);
    }

    /**
     * 获取过期时间还有多少（以CacheKey的单位换算）
     * @param cacheKey
     * @return
     */
    public static Long getExpire(ICacheKey cacheKey){
        return redisTemplate.getExpire(cacheKey.getKey(),cacheKey.getExpireTimeUnit());
    }
    /**
     * 对key设置值
     * @param cacheKey
     * @param value
     */
    public static void set(ICacheKey cacheKey,Object value){
        if (cacheKey.getExpire() > 0){
            redisTemplate.opsForValue().set(cacheKey.getKey(),value,cacheKey.getExpire(),cacheKey.getExpireTimeUnit());
        }else {
            redisTemplate.opsForValue().set(cacheKey.getKey(),value);
        }
    }

    /**
     * 批量设置
     * @param map
     */
    public static void set(Map<ICacheKey,Object> map){
        Map<String,Object> paramMap = new HashMap<>(map.size());
        map.entrySet().stream().forEach(entry->paramMap.put(entry.getKey().getKey(),entry.getValue()));
        redisTemplate.opsForValue().multiSet(paramMap);
        //redisTemPlate不支持一次性设置expire
        map.keySet().forEach(cacheKey ->{
            if (cacheKey.getExpire() > 0){
                redisTemplate.expire(cacheKey.getKey(),cacheKey.getExpire(),cacheKey.getExpireTimeUnit());
            }
        });
    }

    /**
     * 暂时不允许使用
     * @param cacheKey
     * @return
     */
    private static Object get(ICacheKey cacheKey){
        return redisTemplate.opsForValue().get(cacheKey.getKey());
    }

    /**
     * 获取值
     * @param cacheKey
     * @param cls
     * @param <cls>
     * @return
     */
    public static <cls> cls get(ICacheKey cacheKey, Class cls){
        return (cls)get(cacheKey);
    }

    /**
     * 批量获取
     * @param keySet
     * @param cls
     * @param <cls>
     * @return
     */
    public static <cls> List mget(Set<ICacheKey> keySet,Class cls){
        if (keySet != null && keySet.size() > 0){
            List<cls> list = redisTemplate.opsForValue().multiGet(keySet);
            return list;
        }
        return new ArrayList<cls>(0);
    }
    /**
     * 列表从左边push
     * @param cacheKey
     * @param value
     */
    public static void lpush(ICacheKey cacheKey,Object value){
        redisTemplate.opsForList().leftPush(cacheKey.getKey(),value);
    }

    /**
     * 列表从左边推入一个元素
     * @param cacheKey
     * @param values
     */
    public static void lpush(ICacheKey cacheKey, Collection values){
        redisTemplate.opsForList().leftPushAll(cacheKey.getKey(),values);
    }

    /**
     * 列表从右边push
     * @param cacheKey
     * @param value
     */
    public static void rpush(ICacheKey cacheKey,Object value){
        redisTemplate.opsForList().rightPush(cacheKey.getKey(),value);
    }

    /**
     * 列表从右边推入一个元素
     * @param cacheKey
     * @param values
     */
    public static void rpush(ICacheKey cacheKey,Collection values){
        redisTemplate.opsForList().rightPushAll(cacheKey.getKey(),values);
    }

    /**
     * 暂时不允许使用
     * @param cacheKey
     * @return
     */
    private static Object lpop(ICacheKey cacheKey){
        return redisTemplate.opsForList().leftPop(cacheKey.getKey());
    }

    /**
     * 列表从左边弹出一个元素
     * @param cacheKey
     * @param cls
     * @param <cls>
     * @return
     */
    public static <cls> cls lpop(ICacheKey cacheKey,Class cls){
        return (cls)lpop(cacheKey);
    }


    /**
     * 暂时不允许使用
     * @param cacheKey
     * @return
     */
    private static Object rpop(ICacheKey cacheKey){
        return redisTemplate.opsForList().rightPop(cacheKey.getKey());
    }

    /**
     * 列表从右边弹出一个元素
     * @param cacheKey
     * @param cls
     * @param <cls>
     * @return
     */
    public static <cls> cls rpop(ICacheKey cacheKey,Class cls){
        return (cls)rpop(cacheKey);
    }

    /**
     * 暂时不允许使用
     * @param cacheKey
     * @param index
     * @return
     */
    private static Object get(ICacheKey cacheKey,int index){
        return redisTemplate.opsForList().index(cacheKey.getKey(),index);
    }

    /**
     * 获取列表指定位置的元素
     * @param cacheKey
     * @param index
     * @param cls
     * @param <cls>
     * @return
     */
    public static <cls> cls get(ICacheKey cacheKey,int index,Class cls){
        return (cls)get(cacheKey,index);
    }

    /**
     * 删除元素
     * @param cacheKey
     * @param obj
     */
    public static void remove(ICacheKey cacheKey,Object obj){
        remove(cacheKey,obj,1);
    }
    /**
     * 删除count的绝对值个某个元素（count正负区分查找方向，count大于0从左到右，小于0从右往左）
     * @param cacheKey
     * @param obj
     */
    public static void remove(ICacheKey cacheKey,Object obj,Integer count){
        redisTemplate.opsForList().remove(cacheKey.getKey(),count,obj);
    }

    /**
     * 暂时不给用
     * @param cacheKey
     * @param start
     * @param end
     * @return
     */
    private static List<Object> range(ICacheKey cacheKey, int start, int end){
        return redisTemplate.opsForList().range(cacheKey.getKey(),start,end);
    }

    /**
     * 获取列表从start到end的元素
     * @param cacheKey
     * @param start
     * @param end
     * @param cls
     * @param <cls>
     * @return
     */
    public static <cls> List range(ICacheKey cacheKey, int start, int end, Class cls) {
        return (List<cls>)range(cacheKey,start,end);
    }

    /**
     * 对列表下标为index位置的元素设置为value
     * @param cacheKey
     * @param index
     * @param value
     */
    public static void set(ICacheKey cacheKey,int index,Object value){
        redisTemplate.opsForList().set(cacheKey.getKey(),index,value);
    }

    /**
     * 返回列表长度
     * @param cacheKey
     * @return
     */
    public static Long size(ICacheKey cacheKey){
        return redisTemplate.opsForList().size(cacheKey.getKey());
    }

}
