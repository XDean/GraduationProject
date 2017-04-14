package xdean.graduation.workspace.hook;

import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.handler.adapter.IntArrayParamAdapter;
import xdean.graduation.handler.param.handler.adapter.IntParamAdapter;
import xdean.graduation.handler.param.selector.ConvolutionSelector;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.param.selector.ConvolutionSelector.WeightPolicy;
import xdean.graduation.handler.trader.MacdTrader;
import xdean.graduation.handler.trader.common.Trader;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.Context;
import xdean.jex.extra.Pair;

public class MacdHook extends BaseHook<int[], MacdTrader> {

  @Override
  public MacdTrader create(Repo repo) {
    return new MacdTrader(repo) {
      @Override
      public Trader<int[]> setParam(int[] p) {
        return super.setParam(new int[] { p[0], p[0] * p[1] / 100, p[2] });
      }
    };
  }

  @Override
  public int[] getParam() {
    return Context.USE_TIME ? new int[] { 498, 996, 170 } : new int[] { 260, 520, 50 };
  }

  @Override
  public ParamHandler<int[]> getParamHandler() {
    return new IntArrayParamAdapter(
        new IntParamAdapter(10, 1000, 50, 5),
        new IntParamAdapter(200, 200, 50, 5),
        new IntParamAdapter(10, 500, 50, 5));
  }
  
  @Override
  public ParamSelector<int[], Double> getParamSelector() {
    return new ConvolutionSelector(WeightPolicy.CENTER, 1, sqrDis -> sqrDis == 0 ? 0 : 1d / 8);
  }

  @Override
  public void extraColumns(DataWriter<Result<MacdTrader>> writer) {
    writer.addColumn("fast", r -> r.getTrader().getFast());
    writer.addColumn("slow", r -> r.getTrader().getSlow());
    writer.addColumn("dif", r -> r.getTrader().getDif());
    writer.addColumn("macd", r -> r.getTrader().getMacd());
    writer.addColumn("histogram", r -> r.getTrader().getHistogram());
  }

  @Override
  public String formatParam(int[] param) {
    return String.format("f = %d, s = %d, a = %d.", param[0], param[1], param[2]);
  }
  @Override
  public String formatParamResult(Pair<int[], ?> pair) {
    return String.format("With param f = %d, s = %d, a = %d, the %s = %.2f.",
        pair.getLeft()[0], pair.getLeft()[1], pair.getLeft()[2],
        getParamSelectIndexName(), pair.getRight());
  }

  @Override
  public String formatBestParam(Pair<int[], ?> pair) {
    return String.format("Best param is f = %d, s = %d, a = %d, the %s = %.2f.",
        pair.getLeft()[0], pair.getLeft()[1], pair.getLeft()[2],
        getParamSelectIndexName(), pair.getRight());
  }

}
