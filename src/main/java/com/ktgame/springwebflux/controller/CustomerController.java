package com.ktgame.springwebflux.controller;

import com.ktgame.springwebflux.dao.CustomerRepository;
import com.ktgame.springwebflux.domain.Customer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private Sinks.Many<Customer> sink;

    // A -> Flux -> Stream
    // B -> Flux -> Stream
    // Flux.merge -> sink

    @PostConstruct
    public void init() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @GetMapping("/flux")
    Flux<Integer> flux() {
        return Flux.just(1, 2, 3, 4, 5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "/fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    Flux<Integer> fluxStream() {
        return Flux.just(1, 2, 3, 4, 5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "/customer", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    Flux<Customer> findAll() {
        return customerRepository.findAll().delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping("/customer/{id}")
    Mono<Customer> findById(@PathVariable Long id) {
        return customerRepository.findById(id).log();
    }

    @GetMapping("/customer/sse") // 생략 (자동으로 MIME type 설정됨) produces = MediaType.TEXT_EVENT_STREAM_VALUE
    Flux<ServerSentEvent<Customer>> findAllSSE() {
        return sink.asFlux().map(customer -> ServerSentEvent.builder(customer).build()).doOnCancel(() -> sink.asFlux().blockLast());
    }

    @PostMapping("/customer")
    Mono<Customer> save() {
        return customerRepository.save(new Customer("gildong", "Hong")).doOnNext(customer -> sink.tryEmitNext(customer));
    }

}
