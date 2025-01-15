package com.farm.Redis;

import io.lettuce.core.RedisConnectionException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private final TokenRedisService tokenRedisService;

    public Controller(TokenRedisService tokenRedisService) {
        this.tokenRedisService = tokenRedisService;
    }

    @GetMapping("/")
    public String home(){
        return "Hello!";
    }

    @GetMapping("/jwt")
    public String jwt(@RequestParam String id){
//        tokenRedisService.getRedis(id);
//        tokenRedisService.saveObjectTemplate(id);
        try{
            tokenRedisService.saveTemplate(id);
        } catch (RedisConnectionFailureException e){
            return "RedisConnectionFailureException";
        }
        return "Hello!";
    }

}
