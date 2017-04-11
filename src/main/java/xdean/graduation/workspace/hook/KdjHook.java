package xdean.graduation.workspace.hook;

import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.handler.adapter.IntArrayParamAdapter;
import xdean.graduation.handler.param.handler.adapter.IntParamAdapter;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.trader.KdjTrader;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.Context;
import xdean.jex.extra.Pair;

public class KdjHook extends BaseHook<Integer[], KdjTrader> {

  @Override
  public KdjTrader createTrader(Repo repo) {
    return new KdjTrader(repo);
  }

  @Override
  public Integer[] getParam() {
    return Context.USE_TIME ? new Integer[] { 3000, 500 } : new Integer[] { 150, 100 };
  }

  @Override
  public ParamHandler<Integer[]> getParamHandler() {
    return new IntArrayParamAdapter(
        new IntParamAdapter(100, 1000, 100, 10),
        new IntParamAdapter(10, 500, 50, 5));
  }

  @Override
  public ParamSelector<Integer[], Double> getParamSelector() {
    return ParamSelector.natural();// TODO
  }

  @Override
  public void extraColumns(DataWriter<Result<KdjTrader>> sc) {
    sc.addColumn("rsv", r -> r.getTrader().getKdj().getRsv().get());
    sc.addColumn("K", r -> r.getTrader().getKdj().getK());
    sc.addColumn("D", r -> r.getTrader().getKdj().getD());
    sc.addColumn("J", r -> r.getTrader().getKdj().getJ());
  }

  @Override
  public String formatParam(Integer[] param) {
    return String.format("n = %d, l = m = %d.", param[0], param[1]);
  }

  @Override
  public String formatParamResult(Pair<Integer[], ?> pair) {
    return String.format("With param n = %d, l = m =%d, the %s = %.4f.",
        pair.getLeft()[0], pair.getLeft()[1],
        getParamSelectIndexName(), pair.getRight());
  }

  @Override
  public String formatBestParam(Pair<Integer[], ?> pair) {
    return String.format("Best param is n = %d, l = m =%d, the %s = %.4f.",
        pair.getLeft()[0], pair.getLeft()[1],
        getParamSelectIndexName(), pair.getRight());
  }
}