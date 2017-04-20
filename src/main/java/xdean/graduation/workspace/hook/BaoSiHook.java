package xdean.graduation.workspace.hook;

import java.util.Arrays;

import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.trader.BaoSiTrader;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.jex.extra.Pair;

public class BaoSiHook extends BaseHook<double[], BaoSiTrader> {

  @Override
  protected BaoSiTrader create(Repo repo) {
    return new BaoSiTrader(repo);
  }

  @Override
  public double[] getParam() {
    return new double[] { 3.2e-4, -2.2e-4, 1.2 };
  }

  @Override
  public ParamHandler<double[]> getParamHandler() {
    return null;
  }

  @Override
  public void extraColumns(DataWriter<Result<BaoSiTrader>> writer) {
    writer.addColumn("histogram", r -> r.getTrader().getHistogram());
    writer.addColumn("stable", r -> r.getTrader().getStableMacd());
  }

  @Override
  public String formatParam(double[] param) {
    return String.format("param: %s.", Arrays.toString(param));
  }

  @Override
  public String formatParamResult(Pair<double[], ?> pair) {
    return String.format("param: %s, result: %s.", Arrays.toString(pair.getLeft()), pair.getRight());
  }

  @Override
  public String formatBestParam(Pair<double[], ?> pair) {
    return "best" + formatParamResult(pair);
  }
}
