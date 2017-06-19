package xdean.graduation.workspace.hook;

import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.handler.adapter.IntArrayParamAdapter;
import xdean.graduation.handler.param.handler.adapter.IntParamAdapter;
import xdean.graduation.handler.param.selector.ConvolutionSelector;
import xdean.graduation.handler.param.selector.ConvolutionSelector.WeightStrategy;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.trader.MacdTrader;
import xdean.graduation.handler.trader.TraderUtil;
import xdean.graduation.handler.trader.base.PositionStrategy;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.Context;
import xdean.jex.extra.Pair;

public class MacdHook extends BaseHook<int[], MacdTrader> {

  @Override
  public MacdTrader create(Repo repo) {
    MacdTrader macdTrader = new MacdTrader(repo) {
      @Override
      public MacdTrader setParam(int[] p) {
        return super.setParam(new int[] { p[0], (int) (p[0] * p[1] / 100d), p[2] });
      }
    };
    macdTrader.setPositionStrategy(getPositionStrategy());
    macdTrader.addAdditionalPositionHandler(TraderUtil.closeIfOverNight(repo));
    // macdTrader.addAdditionalPositionHandler(TraderUtil.cutLoss(repo, -0.01));
    // macdTrader.addAdditionalPositionHandler(TraderUtil.saveEnarning(repo, 0.01, 0.4));
    // macdTrader.addAdditionalPositionHandler(TraderUtil.saveEnarning(repo, 0.02, 0.6));
    // macdTrader.addAdditionalPositionHandler(TraderUtil.saveEnarning(repo, 0.03, 0.8));
    return macdTrader;
  }

  @Override
  public int[] getParam() {
    return Context.USE_TIME ? new int[] { 500, 200, 400 } : new int[] { 260, 200, 50 };
  }

  @Override
  public ParamHandler<int[]> getParamHandler() {
    return new IntArrayParamAdapter(
        new IntParamAdapter(100, 1000, 25, 5),
        new IntParamAdapter(200, 200, 1, 5),
        new IntParamAdapter(100, 500, 25, 5));
  }

  @Override
  public ParamSelector<int[], Double> getParamSelector() {
    return new ConvolutionSelector(WeightStrategy.AVG, 1, sqrDis -> 1d);
  }

  protected PositionStrategy getPositionStrategy() {
    return PositionStrategy.ALL_OUT;
    // DoubleIndex average = Indexs.average();
    // return PositionStrategy.create(d -> {
    // average.accept(d);
    // double relative = d / average.get();
    // return relative * relative * 0.01;
    // }, d -> {
    // average.accept(d);
    // double relative = d / average.get();
    // return relative * relative * 0.01;
    // });
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
  public String formatParam(int[] p) {
    return String.format("f = %d, s = %d, a = %d.", p[0], (int) (p[0] * p[1] / 100d), p[2]);
  }

  @Override
  public String formatParamResult(Pair<int[], ?> pair) {
    int[] p = pair.getLeft();
    return String.format("With param f = %d, s = %d, a = %d, the %s = %.2f%%.",
        p[0], (int) (p[0] * p[1] / 100d), p[2],
        getParamSelectIndexName(), 100 * (Double) pair.getRight());
  }

  @Override
  public String formatBestParam(Pair<int[], ?> pair) {
    int[] p = pair.getLeft();
    return String.format("Best param is f = %d, s = %d, a = %d, the %s = %.2f%%.",
        p[0], (int) (p[0] * p[1] / 100d), p[2],
        getParamSelectIndexName(), 100 * (Double) pair.getRight());
  }

}
