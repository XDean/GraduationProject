package xdean.graduation.handler.param.selector;

import rx.Observable;
import xdean.jex.extra.Pair;

public interface ParamSelector<Param, Result> {
  Pair<Param, Result> select(Observable<Pair<Param, Result>> ob);

  static <P, R extends Comparable<R>> ParamSelector<P, R> natural() {
    return ob -> ob.scan((a, b) -> b.getRight().compareTo(a.getRight()) > 0 ? b : a)
        .toBlocking()
        .last();
  }
}
