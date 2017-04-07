package xdean.graduation.index.base;

import java.util.function.BiFunction;
import java.util.function.Function;

import rx.functions.Func3;

public class RecursiveIndex<D, T> implements Index<D, T> {

  private final Func3<T, Integer, D, T> func;
  private Function<D, T> initFunc;
  private T value;
  private int count;

  public RecursiveIndex(Function<D, T> initFunc, BiFunction<T, D, T> func) {
    this((T) null, func);
    this.initFunc = initFunc;
  }

  public RecursiveIndex(Function<D, T> initFunc, Func3<T, Integer, D, T> func) {
    this((T) null, func);
    this.initFunc = initFunc;
  }

  public RecursiveIndex(T initValue, BiFunction<T, D, T> func) {
    this(initValue, (t, i, d) -> func.apply(t, d));
  }

  public RecursiveIndex(T initValue, Func3<T, Integer, D, T> func) {
    this.func = func;
    this.value = initValue;
  }

  @Override
  public void accept(D d) {
    if (value == null && initFunc != null) {
      value = initFunc.apply(d);
      initFunc = null;
      count = 1;
    } else {
      value = func.call(value, count++, d);
    }
  }

  @Override
  public T get() {
    return value;
  }

  public static class DoubleRecursiveIndex extends RecursiveIndex<Double, Double> implements DoubleIndex {

    public DoubleRecursiveIndex(Double initValue, BiFunction<Double, Double, Double> func) {
      super(initValue, func);
    }

    public DoubleRecursiveIndex(Double initValue, Func3<Double, Integer, Double, Double> func) {
      super(initValue, func);
    }

    public DoubleRecursiveIndex(Function<Double, Double> initFunc, BiFunction<Double, Double, Double> func) {
      super(initFunc, func);
    }

    public DoubleRecursiveIndex(Function<Double, Double> initFunc, Func3<Double, Integer, Double, Double> func) {
      super(initFunc, func);
    }

    @Override
    public Double get() {
      Double get = super.get();
      return get == null ? 0 : get;
    }
  }
}
