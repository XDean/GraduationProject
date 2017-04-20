package xdean.graduation.workspace;

import static xdean.graduation.workspace.Context.*;
import static xdean.graduation.workspace.Util.*;
import static xdean.jex.util.task.TaskUtil.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import rx.Observable;
import rx.Single;
import xdean.graduation.handler.IndexOperator;
import xdean.graduation.handler.trader.base.Trader;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.jex.extra.Pair;
import xdean.jex.extra.collection.FixedLengthList;
import xdean.jex.extra.rx.op.BothOperator;
import xdean.jex.extra.rx.op.ContinuousGroupOperator;
import xdean.jex.extra.rx.op.FunctionOperator;
import xdean.jex.util.TimeUtil;

@UtilityClass
public class WorkSpace {
  public void main(String[] args) throws IOException, InterruptedException {
    TimeUtil.timeThenPrint(() -> work(), "done in %dms\n");
  }

  void work() {
    Path file = Paths.get("data", "rb170510.jbh.bsv");
    Repo repo = Repo.builder()
        .taxRate(0.0000115)
        .funds(1000 * 1000)
        .canSellShort(true)
        .perLot(1)
        .build();

    switch (getDataSource(file)) {
    case TR:
      if (SELECT_PARAM) {
        paramTR(file, repo).subscribe();
      } else {
        simpleTR(file, repo).subscribe();
      }
      break;
    case JBH:
      if (SELECT_PARAM) {
        // paramSelectJBH(file, repo).subscribe();
        paramIterateJBH(file, repo).subscribe();
      } else {
        simpleJBH(file, repo).subscribe();
      }
      break;
    }
  }

  /*************
   * Ju Bo Hua *
   *************/
  @SneakyThrows(IOException.class)
  Observable<Result<Trader<Object>>> simpleJBH(Path file, Repo repo) {
    Path output = getOutputFile(file);
    uncatch(() -> Files.delete(output));
    Files.createFile(output);
    return getReader().read(file)
        .lift(new ContinuousGroupOperator<>(Order::getDate))
        .map(WorkSpace::skipAndOp)
        .doOnNext(o -> System.out.printf("Calc %s data.\n", o.getLeft()))
        .concatMap(o ->
            indaySave(
                o.getRight(),
                uncheck(() -> Files.newOutputStream(output, StandardOpenOption.APPEND)),
                getHook().createTraderWithParam(repo.copy()))
                .toObservable())
        .doOnNext(p -> System.out.println(getHook().formatIndayResult(p)))
        .lift(FunctionOperator.of(o -> saveDailyData(o, file)));
  }

  Single<Pair<Object, Double>> paramSelectJBH(Path file, Repo repo) {
    Observable<Pair<String, Observable<Order>>> ob = getReader()
        .read(file)
        .lift(new ContinuousGroupOperator<>(Order::getDate))
        .map(WorkSpace::skipAndOp);
    return paramResult(
        ob,
        getHook().getParamHandler(),
        getHook().getParamSelector(),
        (o, p) -> paramToResultJBH(repo.copy(), o, p))
        .doOnSuccess(p -> System.out.println(getHook().formatBestParam(p)));
  }

  @SneakyThrows(IOException.class)
  Observable<Result<Trader<Object>>> paramIterateJBH(Path file, Repo repo) {
    FixedLengthList<Observable<Order>> list = new FixedLengthList<>(5);// Param
    Path output = getOutputFile(file);
    uncatch(() -> Files.delete(output));
    Files.createFile(output);
    return getReader()
        .read(file)
        .lift(new ContinuousGroupOperator<String, Order>(o -> o.getDate()))
        // .takeUntil(p->p.getLeft().equals("20170106"))
        .map(WorkSpace::skipAndOp)
        .doOnNext(p -> list.add(p.getRight()))
        .map(pair ->
            Pair.of(pair,
                paramResult(
                    Observable.from(list)
                        .concatMap(o -> o)
                        .lift(new ContinuousGroupOperator<>(Order::getDate))
                        .<Pair<String, Observable<Order>>> map(p -> p.right(Observable.from(p.getRight()))),
                    getHook().getParamHandler(),
                    getHook().getParamSelector(),
                    (o, p) -> paramToResultJBH(repo.copy(), o, p)
                ).toBlocking().value()))
        // ((name, data), (param, result))
        .doOnNext(p -> System.out.printf("In %s, ", p.getLeft().getLeft()))
        .doOnNext(p -> System.out.println(getHook().formatBestParam(p.getRight())))
        .doOnNext(p -> splitLine(false))
        .lift(new BothOperator<>())
        .doOnNext(p -> System.out.printf("Back test %s with param %s\n",
            p.getRight().getLeft().getLeft(),
            getHook().formatParam(p.getLeft().getRight().getLeft())))
//        .filter(p -> p.getLeft().getRight().getRight() > 0)
        .concatMap(p -> indaySave(
            p.getRight().getLeft().getRight(),
            uncheck(() -> Files.newOutputStream(output, StandardOpenOption.APPEND)),
            getHook().createTrader(repo.copy()).setParam(p.getLeft().getRight().getLeft()))
            .toObservable())
        .doOnNext(p -> System.out.println(getHook().formatIndayResult(p)))
        .lift(FunctionOperator.of(o -> saveDailyData(o, file)));
  }

  private <P> Single<Double> paramToResultJBH(Repo repo, Observable<Pair<String, Observable<Order>>> o,
      P param) {
    return o
        .concatMap(
            pair -> inday(
                pair.getRight(),
                getHook().createTrader(repo.copy()).setParam(param))
                .last()
        )
        .lift(IndexOperator.create(() -> getHook().getResultIndex(false)))
        // .doOnNext(r -> System.out.println(getHook().formatParamResult(Pair.of(param, r))))
        .toSingle();
  }

  private Pair<String, Observable<Order>> skipAndOp(Pair<String, List<Order>> pair) {
    return pair.right(
        Observable.from(pair.getRight())
            .skip(1)
            .lift(Context.DEFAULT_OPERATER));
  }

  /*************
   * Tian Ruan *
   *************/
  @SneakyThrows(IOException.class)
  Single<Result<Trader<Object>>> simpleTR(Path file, Repo repo) {
    return indaySave(
        getReader().read(file),
        Files.newOutputStream(getOutputFile(file)),
        getHook().createTraderWithParam(repo.copy()))
        .doOnSuccess(p -> System.out.println(getHook().formatIndayResult(p)));
  }

  Single<Pair<Object, Double>> paramTR(Path file, Repo repo) {
    return inDayParamResult(
        getReader().read(file),
        getHook().getParamHandler(),
        () -> getHook().createTrader(repo.copy()),
        getHook().getResultIndex(true));
  }
}
