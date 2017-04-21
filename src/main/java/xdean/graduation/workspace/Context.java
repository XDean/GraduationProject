package xdean.graduation.workspace;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;
import rx.Observable.Operator;
import rx.Scheduler;
import xdean.graduation.handler.TimeOperator;
import xdean.graduation.handler.VolumeOperator;
import xdean.graduation.handler.trader.base.PositionStrategy;
import xdean.graduation.handler.trader.base.Trader;
import xdean.graduation.io.writer.CsvSaver;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Order;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.hook.BaoSiHook;
import xdean.graduation.workspace.hook.Hook;
import xdean.graduation.workspace.hook.KdjHook;
import xdean.graduation.workspace.optional.ParamSelectIndex;
import xdean.jex.extra.rx.RxUtil;
import xdean.jex.util.cache.CacheUtil;

@UtilityClass
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class Context {

  @NonFinal
  public static enum DataSource {
    TR, JBH;
  }

  boolean USE_TIME;
  boolean SELECT_PARAM;
  boolean TRADE_WITH_CURRENT_PRICE = false;
  double RISK_FREE = 0.05;
  // boolean CLOSE_OVER_NIGHT = false;
  private Hook<?, ?> hook =
//       new KdjHook();
//       new MacdHook();
      // new MdwHook();
      new BaoSiHook();

  static {
    Object command = System.getProperties().get("sun.java.command");
    List<String> cmd = Arrays.asList(((String) command).split(" "));
    USE_TIME = cmd.contains("-time");
    SELECT_PARAM = cmd.contains("-param");
  }

  Operator<Order, Order> DEFAULT_OPERATER = USE_TIME ?
      new TimeOperator(10 * 1000, 1000 * 30) :
      new VolumeOperator(10);
  PositionStrategy DEFAULT_POLICY = PositionStrategy.ALL_OUT;
  int THREAD_COUNT = 8;
  ParamSelectIndex PARAM_INDEX = ParamSelectIndex.RR;

  @SuppressWarnings("unchecked")
  public <P, T extends Trader<P>> Hook<P, T> getHook() {
    return (Hook<P, T>) hook;
  }

  public <D> DataWriter<D> getWriter(OutputStream output) {
    return new CsvSaver<>(output);
  }

  public <T extends Trader<?>> void defaultColumns(DataWriter<Result<T>> cs) {
    cs.addColumn("date", r -> r.getOrder().getDate());
    cs.addColumn("time", r -> r.getOrder().getTime());
    cs.addColumn("average price", r -> r.getOrder().getAveragePrice());
    cs.addColumn("volume", r -> r.getOrder().getVolume());
    cs.addColumn("price", r -> r.getOrder().getCurrentPrice());
    cs.addColumn("hold", r -> r.getRepo().getHold());
    cs.addColumn("cost", r -> r.getRepo().getCost());
    cs.addColumn("funds", r -> r.getRepo().getFunds());
    cs.addColumn("rr", r -> r.getRepo().getReturnRate());
  }

  public Scheduler getShareScheduler() {
    return CacheUtil.cache(Context.class, Thread.currentThread(), () -> RxUtil.fixedSizeScheduler(THREAD_COUNT));
  }
}
