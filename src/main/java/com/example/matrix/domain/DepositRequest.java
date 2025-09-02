package com.example.matrix.domain;

import com.example.matrix.annotations.Factor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DepositRequest {
    @Factor
    private String currency;
    @Factor
    private long amount;
    @Factor
    private int term;
}
