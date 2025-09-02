package com.example.matrix;

import com.example.matrix.domain.DepositRequest;
import com.example.matrix.domain.DepositTariff;
import com.example.matrix.io.TariffLoader;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DecisionMatrixTest {

    @Test
    public void decision_matrix_rows_loaded_from_file()
    {
        //given
        Path path = Paths.get("src/test/resources/tariffs.txt");
        //when
        Set<DepositTariff> rows = TariffLoader.loadRows(path, DepositTariff.class);
        //then
        assertNotNull(rows);
        assertEquals(3, rows.size());
    }

    @Test
    public void decision_matrix_loaded_from_file()
    {
        //given
        Path path = Paths.get("src/test/resources/tariffs.txt");
        //when
        DecisionMatrixFactory factory = TariffLoader.load(path);
        //then
        assertNotNull(factory);
    }

    @Test
    public void execute_demo_test() {
        DecisionMatrixFactory factory = TariffLoader.load(Paths.get("src/test/resources/tariffs.txt"));
        DecisionMatrix<DepositRequest, DepositTariff> matrix = factory
                .getMatrix(DepositRequest.class, DepositTariff.class);
        DepositRequest aCase = new DepositRequest("RUR", 10000, 12);
        List<DepositTariff> tariffs = matrix.getDecision(aCase);
        assertEquals(1, tariffs.size());
        assertEquals(Double.valueOf(7), tariffs.get(0).getRate());
    }

    @Test
    public void matrix_returns_empty_when_string_factor_differs() {
        DecisionMatrixFactory factory = TariffLoader.load(Paths.get("src/test/resources/tariffs.txt"));
        DecisionMatrix<DepositRequest, DepositTariff> matrix =
                factory.getMatrix(DepositRequest.class, DepositTariff.class);

        var wrongCurrency = new DepositRequest("USD", 10000, 12);
        assertTrue(matrix.getDecision(wrongCurrency).isEmpty());
    }

    //Несколько совпадений + порядок = порядок строк в файле
    @Test
    public void matrix_returns_multiple_matches_in_file_order(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("overlap.txt");
        Files.writeString(f, String.join("\n",
                "t1\tRUR;10000-100000;1-12;5",
                "t2\tRUR;10000-100000;6-12;7"
        ), StandardCharsets.UTF_8, StandardOpenOption.CREATE);

        DecisionMatrixFactory factory = TariffLoader.load(f);
        DecisionMatrix<DepositRequest, DepositTariff> matrix =
                factory.getMatrix(DepositRequest.class, DepositTariff.class);

        var aCase = new DepositRequest("RUR", 10000, 12);
        List<DepositTariff> res = matrix.getDecision(aCase);

        assertEquals(2, res.size());
        // порядок должен соответствовать порядку строк в файле
        assertEquals((Double) 5.0, res.get(0).getRate());
        assertEquals((Double) 7.0, res.get(1).getRate());
    }

    @Test
    public void loader_ignores_comments_and_blank_lines(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("mix.txt");
        Files.writeString(f, String.join("\n",
                "# header comment",
                "",
                "t1\tRUR;10000-100000;1-3;3",
                "   ",
                "# another comment",
                "t2\tRUR;10000-100000;3-6;5",
                "t3\tRUR;10000-100000;6-12;7"
        ), StandardCharsets.UTF_8, StandardOpenOption.CREATE);

        Set<DepositTariff> rows = TariffLoader.loadRows(f, DepositTariff.class);
        assertEquals(3, rows.size());
    }
}
