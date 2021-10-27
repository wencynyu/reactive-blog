package top.yuwenxin.blog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class StaticController {

    @Autowired
    private DatabaseClient client;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper mapper;

    @RequestMapping("index")
    public Mono<String> getIndex(){
        client.sql("select * from t_demo")
                .map(row -> {
                    Demo demo = new Demo();
                    demo.setId((Long)row.get("id"));
                    demo.setName((String)row.get("name"));
                    return demo;
                })
                .all()
                .subscribe(demo -> {
                    try {
                        kafkaTemplate.send("demo-topic", mapper.writeValueAsString(demo));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });
        redisTemplate.opsForValue().set("key", "value").subscribe(System.out::println);
        return Mono.just("hello world!");
    }

    @KafkaListener(id = "demo-consumer", topics = {"demo-topic"})
    public void consumeDemoTopic(String message){
        try {
            Demo demo = mapper.readValue(message, Demo.class);
            System.out.println("demo-consumer consume: " + demo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Data
    static class Demo{
        private Long id;
        private String name;
    }
}
