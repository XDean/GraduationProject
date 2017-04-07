package xdean.graduation.workspace;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import rx.Observable;
import rx.Single;
import xdean.graduation.handler.param.handler.ParamHandler;
import xdean.graduation.handler.param.selector.ParamSelector;
import xdean.graduation.handler.trader.Trader;
import xdean.graduation.index.MaxDrawdown;
import xdean.graduation.index.RepoAnalyser;
import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;
import xdean.graduation.io.reader.DataReader;
import xdean.graduation.io.reader.TianRuan.IF01ExcelReader;
import xdean.graduation.io.writer.DataWriter;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.jex.extra.Pair;
import xdean.jex.extra.rx.ParallelReplayOnSubscribe;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.file.FileUtil;
import xdean.jex.util.task.TaskUtil;

@UtilityClass
public class Util {
  /**
   * Calculate in-day data with the trader.
   * 
   * @param ob
   * @param trader
   * @return Observable of the result by order
   */
  <T extends Trader<?>> Observable<Result<T>> inday(Observable<Order> ob, T trader) {
    Repo repo = trader.getRepo();
    MaxDrawdown md = Indexs.maxDrawdown(true);
    RepoAnalyser ra = new RepoAnalyser();
    return ob.lift(Context.OPERATER)// XXX
        .doOnNext(o -> trader.trade(o))
        .doOnCompleted(() -> {
          repo.close();
          ra.accept(repo);
          md.accept(repo.getReturnRate());
        })
        .doOnNext(e -> ra.accept(repo))
        .doOnNext(e -> md.accept(repo.getReturnRate()))
        // .doOnError(Throwable::printStackTrace)
        .map(o -> new Result<T>(o, repo, md.get(), ra.get(), trader));
  }

  /**
   * In-day calculation and save the output and print the result
   * 
   * @param ob
   * @param output
   * @param trader
   * @return
   */
  <P, T extends Trader<P>> Single<Result<T>> indaySave(Observable<Order> ob, OutputStream output, T trader) {
    return save(
        inday(ob, trader),
        output,
        Context.<P, T> getHook()::extraColumns)
        .last()
        .toSingle()
        .doOnSuccess(Context.<P, T> getHook()::pirntIndayResult);
  }

  <T extends Trader<?>> Observable<Result<T>> save(Observable<Result<T>> ob, OutputStream output) {
    return save(ob, output, null);
  }

  <T extends Trader<?>> Observable<Result<T>> save(Observable<Result<T>> ob, OutputStream output, Consumer<DataWriter<Result<T>>> extraColumn) {
    DataWriter<Result<T>> sc = Context.getWriter(output);
    Context.defaultColumns(sc);
    Optional.ofNullable(extraColumn).ifPresent(c -> c.accept(sc));
    sc.start();
    return ob
        .doOnNext(sc::row)
        .doOnCompleted(sc::end);
  }

  <D, P, T extends Comparable<T>> Single<Pair<P, T>> paramResult(Observable<D> ob,
      ParamHandler<P> handler, BiFunction<Observable<D>, P, Single<T>> func) {
    return paramResult(ob, handler, ParamSelector.natural(), func);
  }

  <D, P, T> Single<Pair<P, T>> paramResult(Observable<D> ob,
      ParamHandler<P> handler, ParamSelector<P, T> selector, BiFunction<Observable<D>, P, Single<T>> func) {
    Observable<D> replayOb = ParallelReplayOnSubscribe.create(ob);
    return handler.select(p -> func.apply(replayOb, p).toBlocking().value(), selector);
  }

  <P, R extends Comparable<R>, T extends Trader<P>> Single<Pair<P, R>> inDayParamResult(Observable<Order> ob,
      ParamHandler<P> handler, Supplier<T> traderFactory, Index<? super Result<T>, R> index) {
    return paramResult(ob, handler, (o, p) -> {
      T trader = traderFactory.get();
      trader.setParam(p);
      return inday(o, trader)
          .doOnNext(index::accept)
          .last()
          .map(r -> index.get())
          .toSingle();
    })
        .doOnSuccess(p -> Context.<P, T> getHook().printParamResult(p));
  }

  Context.DataSource getDataSource(Path p) {
    String fileName = p.getFileName().toString();
    String[] split = fileName.split("\\.");
    return Context.DataSource.valueOf(split[split.length - 2].toUpperCase());
  }

  DataReader<Path, Order> getReader() {
    return CacheUtil.<DataReader<Path, Order>> cache(
        IF01WorkSpace.class,
        "dataReader",
        () -> new DataReader<Path, Order>() {
          @Override
          public Observable<Order> toObservable(Path data) {
            String fileName = data.getFileName().toString();
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Can't handle the file: " + data);
            switch (getDataSource(data)) {
            case TR:
              if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                return new IF01ExcelReader().toObservable(data);
              } else if (fileName.endsWith(".csv")) {
                return new xdean.graduation.io.reader.TianRuan.IF01SplitValueReader(",").toObservable(data);
              } else if (fileName.endsWith(".bsv")) {
                return new xdean.graduation.io.reader.TianRuan.IF01SplitValueReader(" ").toObservable(data);
              } else {
                throw illegalArgumentException;
              }
            case JBH:
              if (fileName.endsWith(".bsv")) {
                return new xdean.graduation.io.reader.JuBoHua.IF01SplitValueReader(" ").toObservable(data);
              } else {
                throw illegalArgumentException;
              }
            default:
              throw illegalArgumentException;
            }
          }
        });
  }

  public Observable<String> lines(Path p) {
    // return Observable.from(CacheUtil.cache(Util.class, p, () ->
    // TaskUtil.uncheck(() -> Files.readAllLines(p))));
    return Observable.from(TaskUtil.uncheck(() -> Files.readAllLines(p)));
  }

  public Path getOutputFile(Path file) {
    return Paths.get("output", FileUtil.getNameWithoutSuffix(file) + ".output.csv");
  }

  public Path getDailyOutputFile(Path file) {
    return Paths.get("output", FileUtil.getNameWithoutSuffix(file) + ".daily.output.csv");
  }
}
