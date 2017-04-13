package xdean.graduation.workspace.hook.mdw;

import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.handler.adapter.IntArrayParamAdapter;
import xdean.graduation.handler.param.handler.adapter.IntParamAdapter;
import xdean.graduation.handler.trader.common.PositionPolicy;
import xdean.graduation.handler.trader.mdw.MdwTrader;
import xdean.graduation.index.base.Index;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.hook.BaseHook;
import xdean.graduation.workspace.optional.ParamSelectIndex;
import xdean.jex.extra.Pair;

public class MdwHook extends BaseHook<int[], MdwTrader> {

  @Override
  public MdwTrader create(Repo repo) {
    return new MdwTrader(repo);
  }
  
  @Override
  protected PositionPolicy getPositionPolicy() {
    return PositionPolicy.ALL_OUT;
  }

  @Override
  public int[] getParam() {
    return new int[] { 0, 0 };
  }

  @Override
  public ParamHandler<int[]> getParamHandler() {
    return new IntArrayParamAdapter(
        new IntParamAdapter(0, 50, 5, 5),
        new IntParamAdapter(0, 50, 5, 5));
  }

  @Override
  public void extraColumns(DataWriter<Result<MdwTrader>> writer) {

  }

  @Override
  public String formatParam(int[] param) {
    return String.format("open = %d, close = %d.", param[0], param[1]);
  }

  @Override
  public String formatParamResult(Pair<int[], ?> pair) {
    return String.format("With param open = %d, close = %d, the %s = %.2f.",
        pair.getLeft()[0], pair.getLeft()[1], getParamSelectIndexName(), pair.getRight());
  }

  @Override
  public String formatBestParam(Pair<int[], ?> pair) {
    return String.format("Best param is open = %d, close = %d, the %s = %.2f.",
        pair.getLeft()[0], pair.getLeft()[1], getParamSelectIndexName(), pair.getRight());
  }

  @Override
  public Index<? super Result<MdwTrader>, Double> getResultIndex(boolean feedAccumulate) {
    return ParamSelectIndex.RR.getIndex(feedAccumulate);
  }

  @Override
  protected String getParamSelectIndexName() {
    return ParamSelectIndex.RR.getName();
  }
}
