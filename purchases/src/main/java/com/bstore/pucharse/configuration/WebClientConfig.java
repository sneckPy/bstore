package com.bstore.pucharse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient productWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8080")
                .filter(ExchangeFilterFunction.ofRequestProcessor(request -> {
                    System.out.println(">>> WebClient está llamando a: "
                            + request.method() + " " + request.url());
                    return Mono.just(request);
                }))
                .build();
    }
}