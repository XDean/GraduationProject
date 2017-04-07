package xdean.graduation.io.reader;

import rx.Observable;

public interface DataReader<C, S> {

  Observable<S> toObservable(C data);
  
}
