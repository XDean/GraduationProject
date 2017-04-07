package xdean.graduation.handler;

import static xdean.jex.util.cache.CacheUtil.cache;

import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import rx.Subscriber;
import xdean.graduation.index.base.Index;
import xdean.jex.extra.rx.SimpleOperator;

/**
 * Use an {@code Index<F,T>} to convert an {@code Observable<F>} to a value T
 * 
 * @author XDean
 *
 * @param <T>
 * @param <F>
 */
@AllArgsConstructor
public class IndexOperator<T, F> extends SimpleOperator<T, F> {

  final Supplier<Index<F, T>> indexFactory;

  @Override
  protected void onNext(Subscriber<? super T> actual, F t) {
    getIndex(actual).accept(t);
  }

  @Override
  protected void onCompleted(Subscriber<? super T> actual) {
    actual.onNext(getIndex(actual).get());
    actual.onCompleted();
  }

  private Index<F, T> getIndex(Subscriber<? super T> s) {
    return cache(this, s, indexFactory);
  }
}
