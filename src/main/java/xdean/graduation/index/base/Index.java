package xdean.graduation.index.base;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Index<D, T> extends Consumer<D>, Supplier<T> {
  default T get(D d) {
    accept(d);
    return get();
  }

  default <N> Index<N, T> newIn(Function<N, D> func) {
    return create(n -> this.accept(func.apply(n)), this);
  }

  default <N> Index<D, N> newOut(Function<T, N> func) {
    return create(this, () -> func.apply(this.get()));
  }

  static <D, T> Index<D, T> create(Consumer<D> C, Supplier<T> S) {
    return new Index<D, T>() {
      @Override
      public void accept(D t) {
        C.accept(t);
      }

      @Override
      public T get() {
        return S.get();
      }
    };
  }
}
