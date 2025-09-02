package com.example.matrix;

public interface DecisionMatrixFactory {
    <CustomCase, CustomDecision> DecisionMatrix<CustomCase, CustomDecision>
    getMatrix(Class<CustomCase> caseType, Class<CustomDecision> decisionType);
}