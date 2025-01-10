package com.farm.Redis;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TokenRedisRepository extends CrudRepository<TokenRedisEntity, String> {

    Optional<TokenRedisEntity> findByAccessToken(String accessToken);

}
