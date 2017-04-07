package xdean.graduation.index.base;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface DoubleIndex extends Index<Double, Double> {

  static <D, T> DoubleIndex create(Consumer<Double> C, Supplier<Double> S) {
    return new DoubleIndex() {
      @Override
      public void accept(Double t) {
        C.accept(t);
      }

      @Override
      public Double get() {
        return S.get();
      }
    };
  }
}
