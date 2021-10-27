package top.yuwenxin.blog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class StaticController {

    @RequestMapping("index")
    public Mono<String> getIndex(){
        return Mono.just("hello world!");
    }
}
