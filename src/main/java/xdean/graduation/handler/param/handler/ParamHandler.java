package xdean.graduation.handler.param.handler;

import java.util.function.Function;

import rx.Single;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.jex.extra.Pair;

public interface ParamHandler<P> {

  default <R extends Comparable<R>> Single<Pair<P, R>> select(Function<P, R> func) {
    return select(func, ParamSelector.natural());
  }

  <Result> Single<Pair<P, Result>> select(Function<P, Result> func, ParamSelector<P, Result> selector);
}
