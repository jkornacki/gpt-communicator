package pl.apps.gptdemo.gptdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class GptDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GptDemoApplication.class, args);
    }


    @Controller("/")
    public static class MvcController {

        @GetMapping
        public String indexPage() {
            return "index";
        }
    }

}
