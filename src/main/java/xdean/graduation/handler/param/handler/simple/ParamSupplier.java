package xdean.graduation.handler.param.handler.simple;

import java.util.function.Function;
import java.util.function.Supplier;

import rx.Observable;
import rx.Single;
import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.param.selector.SelectorOperator;
import xdean.graduation.workspace.Context;
import xdean.jex.extra.Pair;
import xdean.jex.extra.rx.op.ParallelOperator;

public interface ParamSupplier<P> extends ParamHandler<P> {

  Observable<P> getParams();

  @Override
  default <Result> Single<Pair<P, Result>> select(Function<P, Result> func, ParamSelector<P, Result> selector) {
    return getParams()
        .lift(new ParallelOperator<>(Context.getShareScheduler()))
        .map(p -> Pair.of(p, func.apply(p)))
        .nest()
        .lift(new SelectorOperator<>(selector))
        .last()
        .toSingle();
  }

  static <P> ParamSupplier<P> create(Supplier<Observable<P>> sup) {
    return new ParamSupplier<P>() {
      @Override
      public Observable<P> getParams() {
        return sup.get();
      }
    };
  }
}
