package xdean.graduation.io.reader;

import rx.Observable;

public interface DataReader<C, S> {

  Observable<S> read(C data);

  @SuppressWarnings("unchecked")
  default Observable<S> read(C... data) {
    return Observable.from(data)
        .map(this::read)
        .concatMap(o -> o);
  };
}
