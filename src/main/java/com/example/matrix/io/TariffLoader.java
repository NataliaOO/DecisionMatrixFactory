package com.example.matrix.io;


import com.example.matrix.DecisionMatrixFactory;
import com.example.matrix.core.DefaultDecisionMatrixFactory;
import com.example.matrix.reflect.RowMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Отвечает только за чтение и первичный парсинг строк.
 * Маппинг токенов в объект вынесен в RowMapper (SRP).
 */
public final class TariffLoader {

    private TariffLoader() { }

    public static <T> Set<T> loadRows(Path path, Class<T> rowType) {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            Set<T> out = new LinkedHashSet<>();
            lines.map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !s.startsWith("#")) // комментарии
                    .map(TariffLoader::extractDataPart) // отбрасываем префикс "тариф1\t" если есть
                    .map(s -> s.split(";", -1))       // токенизация по ';'
                    .map(tokens -> RowMapper.map(tokens, rowType))
                    .forEach(out::add);
            return out;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + path, e);
        }
    }

    /**
     * Поддержка форматов:
     *  - "тариф1<TAB>RUR;10000-100000;1-3;3"
     *  - "тариф1: RUR;10000-100000;1-3;3"
     *  - "RUR;10000-100000;1-3;3"
     */
    private static String extractDataPart(String line) {
        int tab = line.indexOf('\t');
        if (tab >= 0) return line.substring(tab + 1).trim();
        int colon = line.indexOf(':');
        if (colon >= 0) return line.substring(colon + 1).trim();
        return line;
    }

    public static DecisionMatrixFactory load(Path path) {
        return new DefaultDecisionMatrixFactory(path);
    }
}
