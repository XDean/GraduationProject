package xdean.graduation.handler;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

import xdean.jex.extra.Pair;

public class ParameterSelector<F, T> implements Function<F, T>, Supplier<Pair<F, T>> {

  final Function<F, T> function;
  final Comparator<T> comparator;
  F maxParam;
  T maxResult;

  public ParameterSelector(Function<F, T> function, Comparator<T> comparator) {
    super();
    this.function = function;
    this.comparator = comparator;
  }

  @Override
  public T apply(F f) {
    T t = function.apply(f);
    if (maxResult == null || comparator.compare(t, maxResult) > 0) {
      maxResult = t;
      maxParam = f;
    }
    return t;
  }

  @Override
  public Pair<F, T> get() {
    return Pair.of(maxParam, maxResult);
  }
}
