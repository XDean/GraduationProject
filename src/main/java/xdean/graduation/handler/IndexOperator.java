package xdean.graduation.handler;

import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import rx.Observable.Operator;
import rx.Subscriber;
import xdean.graduation.index.base.Index;
import xdean.jex.extra.rx.op.SimpleOperator;

/**
 * Use an {@code Index<F,T>} to convert an {@code Observable<F>} to a value T
 * 
 * @author XDean
 */
@UtilityClass
public class IndexOperator{
  public <F, T> Operator<F, T> create(Supplier<Index<T, F>> indexFactory) {
    return new SimpleOperator<F, T>(actual -> new Subscriber<T>() {
      Index<T, F> index = indexFactory.get();

      @Override
      public void onNext(T t) {
        index.accept(t);
      }

      @Override
      public void onCompleted() {
        actual.onNext(index.get());
        actual.onCompleted();
      }

      @Override
      public void onError(Throwable e) {
        actual.onError(e);
      }
    });
  }
}
