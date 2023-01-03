package com.ktgame.springwebflux.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor
public class Customer {

    @Id
    private Long id;
    private final String firstName;
    private final String lastName;

    @Override
    public String toString() {
        return String.format("Customer[id=%d, firstName='%s', lastName='%s' ]", id, firstName, lastName);
    }

}
