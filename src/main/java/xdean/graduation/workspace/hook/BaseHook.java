package xdean.graduation.workspace.hook;

import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.trader.Trader;
import xdean.graduation.index.RepoAnalyser;
import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.Context;

public abstract class BaseHook<P, T extends Trader<P>> implements Hook<P, T> {
  @Override
  public String formatIndayResult(Result<T> result) {
    return String.format("Return rate: %.2f%%, annualized: %.2f%%, max drawdown %.2f%%.\n%s\nPay tax: %.2f%%",
        result.getRepo().getReturnRate() * 100,
        Indexs.annualizedReturn().get(result.getRepo().getReturnRate()) * 100,
        result.getMaxDrawdown() * 100,
        RepoAnalyser.toString(result.getAnalysis()),
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