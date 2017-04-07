package xdean.graduation.workspace.hook;

import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.handler.simple.ParamSupplier;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.trader.Trader;
import xdean.graduation.index.RepoAnalyser;
import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.Context;

public abstract class BaseHook<P, T extends Trader<P>> implements Hook<P, T> {
  @Override
  public void pirntIndayResult(Result<T> result) {
    System.out.printf("Return rate: %.2f%%, annualized: %.2f%%, max drawdown %.2f%%.\n",
        result.getRepo().getReturnRate() * 100,
        Indexs.annualizedReturn().get(result.getRepo().getReturnRate()) * 100,
        result.getMaxDrawdown() * 100);
    System.out.println(RepoAnalyser.toString(result.getAnalysis()));
  }

  @Override
  public ParamHandler<P> getParamHandler() {
    return ParamSupplier.create(this::getParams);
  }

  @Override
  public ParamSelector<P, Double> getParamSelector() {
    return ParamSelector.natural();
  }

  @Override
  public Index<? super Result<T>, Double> getResultIndex(boolean feedAccumulate) {
    return Context.PARAM_INDEX.index.apply(feedAccumulate);
  }

  protected String getParamSelectIndexName() {
    return "rr/md";
  }
}