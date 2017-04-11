package xdean.graduation.workspace.hook;

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

  ParamHandler<P> getParamHandler();

  ParamSelector<P, Double> getParamSelector();

  Index<? super Result<T>, Double> getResultIndex(boolean feedAccumulate);

  void extraColumns(DataWriter<Result<T>> writer);

  String formatParam(P param);

  String formatParamResult(Pair<P, ?> pair);

  String formatBestParam(Pair<P, ?> pair);

  String formatIndayResult(Result<T> result);
}