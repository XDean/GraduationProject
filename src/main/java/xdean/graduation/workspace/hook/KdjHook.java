package xdean.graduation.workspace.hook;

import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.handler.adapter.IntArrayParamAdapter;
import xdean.graduation.handler.param.handler.adapter.IntParamAdapter;
import xdean.graduation.handler.param.selector.ConvolutionSelector;
import xdean.graduation.handler.param.selector.ConvolutionSelector.WeightStrategy;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.trader.KdjTrader;
import xdean.graduation.handler.trader.TraderUtil;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.Context;
import xdean.jex.extra.Pair;

public class KdjHook extends BaseHook<int[], KdjTrader> {

  @Override
  public KdjTrader create(Repo repo) {
    KdjTrader kdjTrader = new KdjTrader(repo);
    kdjTrader.addAdditionalPositionHandler(TraderUtil.closeIfOverNight(repo));
    return kdjTrader;
  }

  @Override
  public int[] getParam() {
    return Context.USE_TIME ? new int[] { 3000, 500 } : new int[] { 150, 100 };
  }

  @Override
  public ParamHandler<int[]> getParamHandler() {
    return new IntArrayParamAdapter(
        new IntParamAdapter(100, 1000, 100, 10),
        new IntParamAdapter(10, 500, 50, 5));
  }

  @Override
  public ParamSelector<int[], Double> getParamSelector() {
    return new ConvolutionSelector(WeightStrategy.AVG, 1, sqrDis -> sqrDis == 0 ? 0 : 1d);
  }

  @Override
  public void extraColumns(DataWriter<Result<KdjTrader>> sc) {
    sc.addColumn("rsv", r -> r.getTrader().getRsv());
    sc.addColumn("K", r -> r.getTrader().getK());
    sc.addColumn("D", r -> r.getTrader().getD());
    sc.addColumn("J", r -> r.getTrader().getJ());
  }

  @Override
  public String formatParam(int[] param) {
    return String.format("n = %d, l = m = %d.", param[0], param[1]);
  }

  @Override
  public String formatParamResult(Pair<int[], ?> pair) {
    return String.format("With param n = %d, l = m =%d, the %s = %.2f%%.%s",
        pair.getLeft()[0], pair.getLeft()[1],
        getParamSelectIndexName(), (Double) pair.getRight() * 100, Thread.currentThread());
  }

  @Override
  public String formatBestParam(Pair<int[], ?> pair) {
    return String.format("Best param is n = %d, l = m =%d, the %s = %.2f%%.",
        pair.getLeft()[0], pair.getLeft()[1],
        getParamSelectIndexName(), (Double) pair.getRight() * 100);
  }
}