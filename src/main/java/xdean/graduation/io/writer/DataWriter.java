package xdean.graduation.io.writer;

import java.util.function.Function;
import java.util.function.Supplier;

import xdean.graduation.index.base.Index;

public interface DataWriter<D> {
  void start();

  void row(D data);

  void end();

  boolean addColumn(String columnName, Function<D, ?> columnFactory);

  default boolean addColumn(String columnName, Supplier<?> columnFactory) {
    return addColumn(columnName, d -> columnFactory.get());
  }

  default boolean addColumn(String columnName, Index<?,?> index){
    return addColumn(columnName, d -> index.get().toString());
  }
  
  default boolean addColumn(Index<?, ?> index) {
    return addColumn(index.getClass().getSimpleName(), index);
  }
}
