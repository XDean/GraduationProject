package xdean.graduation.workspace;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;
import rx.Observable.Operator;
import rx.Scheduler;
import xdean.graduation.handler.TimeOperator;
import xdean.graduation.handler.VolumeOperator;
import xdean.graduation.handler.trader.Trader;
import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;
import xdean.graduation.io.writer.CsvSaver;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Order;
import xdean.graduation.model.Result;
import xdean.graduation.workspace.hook.Hook;
import xdean.graduation.workspace.hook.MacdHook;
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
  private Hook<?, ?> hook =
      // new KdjHook();
      new MacdHook();
  // new MdwHook();

  static {
    Object command = System.getProperties().get("sun.java.command");
    List<String> cmd = Arrays.asList(((String) command).split(" "));
    if (cmd.contains("-time")) {
      USE_TIME = true;
    } else {
      USE_TIME = false;
    }
    if (cmd.contains("-param")) {
      SELECT_PARAM = true;
    } else {
      SELECT_PARAM = false;
    }
  }

  Operator<Order, Order> OPERATER = USE_TIME ?
      new TimeOperator(1000, 1000 * 60 * 60) :
      new VolumeOperator(10);
  int THREAD_COUNT = USE_TIME ? 8 : 8;
  ParamSelectIndex PARAM_INDEX = ParamSelectIndex.RR;

  @SuppressWarnings("unchecked")
  public <P, T extends Trader<P>> Hook<P, T> getHook() {
    return (Hook<P, T>) hook;
  }

  public <D> DataWriter<D> getWriter(OutputStream output) {
    return new CsvSaver<>(output);
  }

  public <T extends Trader<?>> void defaultColumns(DataWriter<Result<T>> cs) {
    cs.addColumn("time stamp", r -> r.getOrder().getTime());
    cs.addColumn("average price", r -> r.getOrder().getAveragePrice());
    cs.addColumn("volume", r -> r.getOrder().getVolume());
    cs.addColumn("price", r -> r.getOrder().getCurrentPrice());
    cs.addColumn("hold", r -> r.getRepo().getHold());
    cs.addColumn("cost", r -> r.getRepo().getCost());
    cs.addColumn("funds", r -> r.getRepo().getFunds());
    cs.addColumn("rr", r -> r.getRepo().getReturnRate());
    cs.addColumn("max drawdown", r -> r.getMaxDrawdown());
  }

  public Scheduler getShareScheduler() {
    return CacheUtil.cache(Context.class, Thread.currentThread(), () -> RxUtil.fixedSizeScheduler(THREAD_COUNT));
  }

  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
  public enum ParamSelectIndex {
    RR("rr",
        acc -> Indexs.accumulateReturnRate(acc).newIn(r -> r.getRepo().getReturnRate())),
    RRMD("rr/md",
        acc -> Indexs.rrMaxDrawdown(acc)
            .newOut(d -> Double.isNaN(d) || !Double.isFinite(d) ? 0 : d)
            .newIn(r -> r.getRepo().getReturnRate()));

    String name;
    Function<Boolean, Index<? super Result<?>, Double>> index;
  }
}
