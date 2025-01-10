package com.farm.Redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenRedisService {


    private final TokenRedisRepository tokenRedisRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private RedisTemplate<String, Object> redisTemplate;

    public TokenRedisService(TokenRedisRepository tokenRedisRepository, JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.tokenRedisRepository = tokenRedisRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    public void getRedis(String userId){
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        tokenRedisRepository.save(new TokenRedisEntity(userId, accessToken));
        Optional<TokenRedisEntity> token = tokenRedisRepository.findByAccessToken(accessToken);

        log.info("token = {}", token.get());

        // TTL 적용: `token:userId` Set에 추가
        redisTemplate.opsForSet().add("token", userId);  // Set에 사용자 추가
        redisTemplate.expire("token", 60, TimeUnit.SECONDS);  // TTL 설정 (60초)

        redisTemplate.opsForValue().get("token:" + accessToken);
        redisTemplate.opsForValue().set("token:" + accessToken, accessToken, Duration.ofSeconds(60));

        redisTemplate.opsForValue().get("token:accessToken");
        redisTemplate.opsForValue().set("token:accessToken", accessToken, Duration.ofSeconds(60));

        /**
         *  자꾸 만료되었는데도 키가 남아있음 set으로 저장한 키들은 삭제되는데 KEYS * 치면 token:accessToken 같은
         *  다른 키 값들이 많이 나옴 객체로 저장 하번 해보기
         *
         */
    }

    public void saveTemplate(String userId){
        String accessToken = jwtTokenProvider.createAccessToken(userId);

        saveAccessToken(userId, accessToken, 60); // accessToken 저장
        String res = getAccessToken(userId); // 키값 정보 뺴오기
        log.info("res = {}", res);

    }


    public void saveAccessToken(String userId, String accessToken, long accessTokenTTL) {
        redisTemplate.opsForValue().set("accessToken:" + userId, accessToken, Duration.ofSeconds(accessTokenTTL));
    }

    public String getAccessToken(String userId) {
        return (String) redisTemplate.opsForValue().get("accessToken:" + userId);
    }

    // 데이터 저장 + TTL 설정
    public void saveDataWithTTL(String key, Object value, long ttlInSeconds) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlInSeconds));
    }

    // 데이터 조회
    public Object getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // TTL 확인
    public Long getTTL(String key) {
        return redisTemplate.getExpire(key);
    }

    // TTL 갱신
    public void setTTL(String key, long ttlInSeconds) {
        redisTemplate.expire(key, Duration.ofSeconds(ttlInSeconds));
    }

    // 데이터 삭제
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

}
