# DecisionMatrixFactory

Лёгкий Java-движок для **правил-решений по таблице/матрице**.  
Правила описываются в текстовом файле (например, `src/test/resources/tariffs.txt`), а затем
через фабрику строится `DecisionMatrix<Case, Decision>` для поиска подходящих вариантов.
