package bunny.boardhole.controller;

import bunny.boardhole.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final HelloService helloService;

    HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/hello")
    public String sayHello() {
        return this.helloService.sayHello();
    }

    @PostMapping("/hello-post")
    public String sayHelloPost() {
        return "Hello, World! This is a POST request.";
    }

}
