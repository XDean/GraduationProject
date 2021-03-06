package xdean.graduation.workspace.hook;

import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.trader.base.Trader;
import xdean.graduation.index.base.Index;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.Context;

public abstract class BaseHook<P, T extends Trader<P>> implements Hook<P, T> {

  @Override
  public final T createTrader(Repo repo) {
    T t = create(repo);
//    t.setPositionStrategy(getPositionStrategy());
    return t;
  }

  protected abstract T create(Repo repo);

  @Override
  public String formatIndayResult(Result<T> result) {
    return String.format("%s\n"
        + "Turnover: %.2f%%\n"
        + "Pay tax: %.2f%%",
        result.getAnalysis().toIndayString(),
        100 * result.getRepo().getTurnOverRate(),
        100 * result.getRepo().getPayTaxRate());
  }

  @Override
  public ParamSelector<P, Double> getParamSelector() {
    return ParamSelector.natural();
  }

  @Override
  public Index<? super Result<T>, Double> getResultIndex(boolean feedAccumulate) {
    return Context.PARAM_INDEX.getIndex(feedAccumulate);
  }

  protected String getParamSelectIndexName() {
    return Context.PARAM_INDEX.getName();
  }
}