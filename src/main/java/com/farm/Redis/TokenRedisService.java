package com.farm.Redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Autowired
    private ObjectMapper objectMapper;

    public TokenRedisService(TokenRedisRepository tokenRedisRepository, JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.tokenRedisRepository = tokenRedisRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    // tokenRedisRepository로 저장
    public void getRedis(String userId){
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        tokenRedisRepository.save(new TokenRedisEntity(userId, accessToken));
        Optional<TokenRedisEntity> token = tokenRedisRepository.findByAccessToken(accessToken);

        log.info("token = {}", token.get());

        /**
         *  자꾸 만료되었는데도 키가 남아있음 set으로 저장한 키들은 삭제되는데 KEYS * 치면 token:accessToken 같은
         *  다른 키 값들이 많이 나옴 객체로 저장 하번 해보기
         *
         *  객체로 저장을 하니깐 특정 토큰만 꺼내기 힘듬 하나씩 저장 하기로 함 
         *  accessToken:{userId} 식으로 값에 토큰을 넣음 그러면 RefreshToken은 RefreshToken:{userId} 이런식으로
         */
    }

    // 하니씩 저장
    public void saveTemplate(String userId){
        String accessToken = jwtTokenProvider.createAccessToken(userId);

        saveAccessToken(userId, accessToken, 60); // accessToken 저장
        String res = getAccessToken(userId); // 키값 정보 뺴오기
        log.info("res = {}", res);

        Long res3 = getTTLAccess(userId); // TTL 시간 확인
        log.info("res3 = {}", res3);

        setTTLAccess(userId, 30);
        log.info("==============시간 수정============");

        Long res4 = getTTLAccess(userId); // TTL 시간 확인
        log.info("res4 = {}", res4);

//        deleteDataAccess(userId); // 데이터 삭제
    }

    // accessToken 저장
    public void saveAccessToken(String userId, String accessToken, long accessTokenTTL) {
        redisTemplate.opsForValue().set("accessToken:" + userId, accessToken, Duration.ofSeconds(accessTokenTTL));
    }

    // accessToken 조회
    public String getAccessToken(String userId) {
        return (String) redisTemplate.opsForValue().get("accessToken:" + userId);
    }

    // accessToken TTL 조회
    public Long getTTLAccess(String key) {
        return redisTemplate.getExpire("accessToken:" + key);
    }

    // accessToken TTL 갱신
    public void setTTLAccess(String key, long ttlInSeconds) {
        redisTemplate.expire("accessToken:" + key, Duration.ofSeconds(ttlInSeconds));
    }

    // accessToken 삭제
    public void deleteDataAccess(String key) {
        redisTemplate.delete("accessToken:" + key);
    }


    // 객체로 저장
    public void saveObjectTemplate(String userId){
        String accessToken = jwtTokenProvider.createAccessToken(userId);

        saveDataWithTTL(userId, new TokenRedis(userId, accessToken), 60); // 데이터 저장 + TTL 설정
        Object res2 = getData(userId);
        log.info("res2 = {}", res2);

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
