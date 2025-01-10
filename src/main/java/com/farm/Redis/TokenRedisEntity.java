package com.farm.Redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 *  @AllArgsConstructor 어노테이션은 클래스의 모든 필드 값을 파라미터로 받는 생성자를 자동으로 생성한다.
 *  이 어노테이션을 사용하면, 클래스의 모든 필드를 한 번에 초기화할 수 있다.
 */

@Data
@RedisHash(value = "token", timeToLive = 60)
public class TokenRedisEntity {

    @Id
    private String id;

    @Indexed
    private String accessToken;

//    private String refreshToken;

    public TokenRedisEntity(String id, String accessToken) {
        this.id = id;
        this.accessToken = accessToken;
    }


}
