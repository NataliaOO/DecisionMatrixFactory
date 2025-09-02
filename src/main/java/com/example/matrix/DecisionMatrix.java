package com.example.matrix;

import java.util.List;

public interface DecisionMatrix<CustomCase, CustomDecision> {
    List<CustomDecision> getDecision(CustomCase byCase);
}
