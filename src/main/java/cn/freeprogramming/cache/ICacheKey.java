package cn.freeprogramming.cache;

import cn.freeprogramming.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 缓存Key包装
 * @Author humorchen
 * @Date 2021/12/11
 */
public abstract class ICacheKey {
    public static final String SEPARATOR = ":";
    public static final Integer DEFAULT_EXPIRE = -1;
    /**
     * 前缀
     */
    private String prefix;
    /**
     * 过期时间
     */
    public Integer expire = DEFAULT_EXPIRE;
    /**
     * 过期时间单位(默认毫秒)
     */
    private TimeUnit expireTimeUnit = TimeUnit.MILLISECONDS;


    public Integer getExpire() {
        return expire;
    }

    public void setExpire(Integer expire) {
        this.expire = expire;
    }

    public TimeUnit getExpireTimeUnit() {
        return expireTimeUnit;
    }

    public void setExpireTimeUnit(TimeUnit expireTimeUnit) {
        this.expireTimeUnit = expireTimeUnit;
    }

    public String getPrefix() {
        if (prefix == null){
            prefix = this.getClass().getName().replace("cn.freeprogramming","fpa").replace(".",SEPARATOR);
        }
        return prefix;
    }

    /**
     * 设置后缀（子类必须实现）
     */
    public abstract String getSuffix();



    /**
     * 获得最终的键
     * @return
     */
    public String getKey(){
        StringBuilder builder = new StringBuilder(getPrefix());
        String suffix = getSuffix();
        if (!StringUtils.isEmpty(suffix)){
            builder.append(SEPARATOR);
            builder.append(suffix);
        }
        return builder.toString();
    }

    @Override
    public final String toString() {
        return getKey();
    }

}
