package xdean.graduation.workspace.hook;

import static xdean.jex.extra.rx.RxUtil.*;
import rx.Observable;
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

public class KdjHook extends BaseHook<int[], KdjTrader> {

  @Override
  public KdjTrader createTrader(Repo repo) {
    return new KdjTrader(repo);
  }

  @Override
  public int[] getParam() {
    return Context.USE_TIME ? new int[] { 3000, 500 } : new int[] { 150, 100 };
  }

  @Override
  public Observable<int[]> getParams() {
    return (Context.USE_TIME ?
        cross(range(1000, 5000, 400), range(100, 1000, 100)) :
        cross(range(100, 500, 50), range(10, 200, 20)))
        .map(p -> new int[] { p.getLeft(), p.getRight() });
  }

  @Override
  public ParamHandler<int[]> getParamHandler() {
    return new IntArrayParamAdapter(
        new IntParamAdapter(100, 1000, 100, 10),
        new IntParamAdapter(10, 500, 50, 5));
  }

  @Override
  public ParamSelector<int[], Double> getParamSelector() {
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
  public void printParam(Pair<int[], ?> pair) {
    System.out.printf("With n = %d, l = m = %d, the %s = %.2f. %s\n",
        pair.getLeft()[0], pair.getLeft()[1], getParamSelectIndexName(),
        pair.getRight(), Thread.currentThread());
  }

  @Override
  public void printParamResult(Pair<int[], ?> pair) {
    System.out.printf("Best param is n = %d, l = m =%d, the %s = %.2f.\n",
        pair.getLeft()[0], pair.getLeft()[1],
        getParamSelectIndexName(), pair.getRight());
  }
}