package com.example.matrix.domain;

import com.example.matrix.annotations.Decision;
import com.example.matrix.annotations.Factor;
import com.example.matrix.model.Interval;
import lombok.Data;

@Data
public class DepositTariff {
    @Factor
    private String currency;
    @Factor
    private Interval<Long> amount;
    @Factor
    private Interval<Integer> term;
    @Decision
    private Double rate;
}
