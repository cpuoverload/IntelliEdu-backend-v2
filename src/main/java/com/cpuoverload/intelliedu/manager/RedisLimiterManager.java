package com.cpuoverload.intelliedu.manager;

import com.cpuoverload.intelliedu.exception.BusinessException;
import com.cpuoverload.intelliedu.exception.Err;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    public void doRateLimit(String key) {
        // 获取 Redis 中的限流器，如果不存在会自动创建
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // 设置限流规则，只有当 Redis 中没有此 key 的限流器时才会生效
        // 所以修改 rate 可能不生效，需要用 rateLimiter.delete() 删除现有的限流器
        rateLimiter.trySetRate(RateType.OVERALL, 1, 3, RateIntervalUnit.SECONDS); // rate 表示每单位时间允许的请求数，rateInterval 表示时间数量

        // 尝试从限流器中获取 1 个许可证（即尝试通过限流）
        boolean canAcquire = rateLimiter.tryAcquire(1);

        if (!canAcquire) {
            throw new BusinessException(Err.TOO_MANY_REQUEST);
        }
    }
}
