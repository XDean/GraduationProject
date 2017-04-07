package xdean.graduation.workspace.hook.mdw;

import static xdean.jex.extra.rx.RxUtil.*;
import rx.Observable;
import xdean.graduation.handler.trader.mdw.MdwTrader;
import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.hook.BaseHook;
import xdean.jex.extra.Pair;

public class MdwHook extends BaseHook<int[], MdwTrader> {

  @Override
  public MdwTrader createTrader(Repo repo) {
    return new MdwTrader(repo);
  }

  @Override
  public int[] getParam() {
    return new int[] { 0, 0 };
  }

  @Override
  public Observable<int[]> getParams() {
    return cross(range(0, 20, 1), range(0, 5, 1))
        .map(p -> new int[] { p.getLeft(), p.getRight() });
  }

  @Override
  public void extraColumns(DataWriter<Result<MdwTrader>> writer) {

  }

  @Override
  public void printParam(Pair<int[], ?> pair) {
    System.out.printf("With open = %d, close = %d, the %s = %.2f. %s\n",
        pair.getLeft()[0], pair.getLeft()[1], getParamSelectIndexName(), pair.getRight(), Thread.currentThread());
  }

  @Override
  public void printParamResult(Pair<int[], ?> pair) {
    System.out.printf("Best param is open = %d, close = %d, the %s = %.2f.\n",
        pair.getLeft()[0], pair.getLeft()[1], getParamSelectIndexName(), pair.getRight());
  }

  @Override
  public Index<Result<MdwTrader>, Double> getResultIndex(boolean feedAccumulate) {
    return Indexs.accumulateReturnRate(feedAccumulate).newIn(r -> r.getRepo().getReturnRate());
  }
  
  @Override
  protected String getParamSelectIndexName() {
    return "rr";
  }
}
