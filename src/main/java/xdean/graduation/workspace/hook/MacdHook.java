package xdean.graduation.workspace.hook;

import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.handler.adapter.IntArrayParamAdapter;
import xdean.graduation.handler.param.handler.adapter.IntParamAdapter;
import xdean.graduation.handler.trader.MacdTrader;
import xdean.graduation.handler.trader.Trader;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.Context;
import xdean.jex.extra.Pair;

public class MacdHook extends BaseHook<Integer[], MacdTrader> {

  @Override
  public MacdTrader createTrader(Repo repo) {
    return new MacdTrader(repo) {
      @Override
      public Trader<Integer[]> setParam(Integer[] p) {
        return super.setParam(new Integer[] { p[0], p[0] * p[1] / 100, p[2] });
      }
    };
  }

  @Override
  public Integer[] getParam() {
    return Context.USE_TIME ? new Integer[] { 3600, 7200, 500 } : new Integer[] { 260, 520, 50 };
  }

  @Override
  public ParamHandler<Integer[]> getParamHandler() {
    return new IntArrayParamAdapter(
        new IntParamAdapter(10, 500, 50, 5),
        new IntParamAdapter(200, 200, 50, 5),
        new IntParamAdapter(10, 500, 50, 5));
  }

  @Override
  public void extraColumns(DataWriter<Result<MacdTrader>> writer) {
    writer.addColumn("fast", r -> r.getTrader().getMacd().getFast());
    writer.addColumn("slow", r -> r.getTrader().getMacd().getSlow());
    writer.addColumn("dif", r -> r.getTrader().getMacd().getDif());
    writer.addColumn("macd", r -> r.getTrader().getMacd().getMacd());
    writer.addColumn("histogram", r -> r.getTrader().getMacd().getHistogram());
  }

  @Override
  public String formatParam(Integer[] param) {
    return String.format("f = %d, s = %d, a = %d.", param[0], param[1], param[2]);
  }

  @Override
  public String formatParamResult(Pair<Integer[], ?> pair) {
    return String.format("With param f = %d, s = %d, a = %d, the %s = %.2f.",
        pair.getLeft()[0], pair.getLeft()[1], pair.getLeft()[2],
        getParamSelectIndexName(), pair.getRight());
  }

  @Override
  public String formatBestParam(Pair<Integer[], ?> pair) {
    return String.format("Best param is f = %d, s = %d, a = %d, the %s = %.2f.",
        pair.getLeft()[0], pair.getLeft()[1], pair.getLeft()[2],
        getParamSelectIndexName(), pair.getRight());
  }

}
