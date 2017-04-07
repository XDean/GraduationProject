package xdean.graduation.io.reader;

import rx.Observable;

public abstract class BaseReader<C, S> implements DataReader<C, S> {

  @Override
  public Observable<S> toObservable(C data) {
    return convert(Observable.just(data));
  }

  protected abstract Observable<S> convert(Observable<C> o);
}
