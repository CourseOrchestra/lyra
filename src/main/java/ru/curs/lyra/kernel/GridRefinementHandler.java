package ru.curs.lyra.kernel;

import ru.curs.celesta.dbutils.BasicCursor;

import java.util.function.Consumer;

@FunctionalInterface
public interface GridRefinementHandler extends Consumer<BasicGridForm<? extends BasicCursor>> {
}
