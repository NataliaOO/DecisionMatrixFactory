package com.example.matrix.core;

import com.example.matrix.DecisionMatrix;
import com.example.matrix.DecisionMatrixFactory;
import com.example.matrix.io.TariffLoader;

import java.nio.file.Path;
import java.util.Set;

public final class DefaultDecisionMatrixFactory implements DecisionMatrixFactory {
    private final Path sourcePath;

    public DefaultDecisionMatrixFactory(Path sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Override
    public <CustomCase, CustomDecision> DecisionMatrix<CustomCase, CustomDecision> getMatrix(
            Class<CustomCase> caseType, Class<CustomDecision> decisionType) {

        // Загружаем все строки (решения) нужного типа из файла.
        Set<CustomDecision> rows = TariffLoader.loadRows(sourcePath, decisionType);
        return new DefaultDecisionMatrix<>(caseType, decisionType, rows);
    }
}
