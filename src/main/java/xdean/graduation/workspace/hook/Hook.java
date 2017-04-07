package xdean.graduation.workspace.hook;

import rx.Observable;
import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.trader.Trader;
import xdean.graduation.index.base.Index;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.jex.extra.Pair;

public interface Hook<P, T extends Trader<P>> {

  T createTrader(Repo repo);

  default T createTraderWithParam(Repo repo) {
    T trader = createTrader(repo);
    trader.setParam(getParam());
    return trader;
  }

  P getParam();

  Observable<P> getParams();

  ParamHandler<P> getParamHandler();

  ParamSelector<P, Double> getParamSelector();

  Index<? super Result<T>, Double> getResultIndex(boolean feedAccumulate);

  void extraColumns(DataWriter<Result<T>> writer);

  void printParam(Pair<P, ?> pair);

  void printParamResult(Pair<P, ?> pair);

  void pirntIndayResult(Result<T> result);
}