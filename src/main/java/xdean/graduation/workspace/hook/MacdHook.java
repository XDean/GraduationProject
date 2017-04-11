package xdean.graduation.workspace.hook;

import static xdean.jex.extra.rx.RxUtil.*;
import rx.Observable;
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

public class MacdHook extends BaseHook<int[], MacdTrader> {

  @Override
  public MacdTrader createTrader(Repo repo) {
    return new MacdTrader(repo) {
      @Override
      public Trader<int[]> setParam(int[] p) {
        return super.setParam(new int[] { p[0], p[0] * p[1] / 100, p[2] });
      }
    };
  }

  @Override
  public int[] getParam() {
    return Context.USE_TIME ? new int[] { 3600, 7200, 500 } : new int[] { 260, 520, 50 };
  }

  @Override
  public Observable<int[]> getParams() {
    return (Context.USE_TIME ?
        cross(range(2000, 4000, 200), range(500, 1500, 100)) :
        cross(range(200, 400, 20), range(50, 150, 10)))
        .map(p -> new int[] { p.getLeft(), p.getLeft() * 2, p.getRight() });
  }

  @Override
  public ParamHandler<int[]> getParamHandler() {
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
