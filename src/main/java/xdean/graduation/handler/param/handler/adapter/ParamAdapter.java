package xdean.graduation.handler.param.handler.adapter;

import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.Single;
import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.param.selector.SelectorOperator;
import xdean.graduation.workspace.Context;
import xdean.jex.extra.Pair;
import xdean.jex.extra.rx.ParallelOperator;

public interface ParamAdapter<P, T> extends ParamHandler<P> {
  /**
   * Get initial parameters and the next precision.
   * 
   * @return
   */
  Pair<Observable<P>, T> getParams();

  /**
   * Get parameters with specify precision
   * 
   * @param paramResult
   *          last parameters' result
   * @param lastPricision
   *          current precision
   * @return Pair(New parameters, next precision), if the next precision equals
   *         current precision, that means no more parameter can select
   */
  Pair<Observable<P>, T> getParams(P paramResult, T precision);

  @Override
  default <Result> Single<Pair<P, Result>> select(Function<P, Result> func, ParamSelector<P, Result> selector) {
    return Single.create(s -> {
      Pair<Observable<P>, T> pair = getParams();
      Pair<P, Result> result = null;
      T oldPrecision = null;
      P bestParam = null;
      while (pair.getRight().equals(oldPrecision) == false) {
        result = pair.getLeft()
            .lift(new ParallelOperator<>(Context.getShareScheduler()))
            .map(i -> Pair.of(i, func.apply(i)))
            .nest()
            .lift(new SelectorOperator<>(selector))
            .toBlocking()
            .last();
        bestParam = result.getLeft();
        oldPrecision = pair.getRight();

        Log.log.debug("Best param is {}, result is {}, Next precision {}.",
            bestParam, result.getRight(), oldPrecision);
        pair = getParams(bestParam, pair.getRight());
      }
      s.onSuccess(result);
    });
  }

  @Slf4j
  static class Log {
  }
}
