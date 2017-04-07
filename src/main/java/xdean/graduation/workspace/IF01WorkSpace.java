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
import xdean.graduation.handler.trader.Trader;
import xdean.graduation.index.MaxDrawdown;
import xdean.graduation.index.RepoAnalyser;
import xdean.graduation.index.base.DoubleIndex;
import xdean.graduation.index.base.Indexs;
import xdean.graduation.io.writer.CsvSaver;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;
import xdean.graduation.model.Result;
import xdean.jex.extra.Pair;
import xdean.jex.extra.rx.ContinuousGroupOperator;
import xdean.jex.util.TimeUtil;

@UtilityClass
public class IF01WorkSpace {
  public void main(String[] args) throws IOException, InterruptedException {
    TimeUtil.timeThenPrint(() -> work(), "done in %dms\n");
  }

  void work() {
    Path file = Paths.get("data", "IF17S1.jbh.bsv");
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
  Observable<Integer> simpleJBH(Path file, Repo repo) {

    DoubleIndex accumulRR = Indexs.product();
    DoubleIndex accumulTax = Indexs.sum();
    DoubleIndex annualRR = Indexs.annualizedReturn();
    DoubleIndex annualSR = Indexs.annualizedSharpRatio(RISK_FREE, false);
    DoubleIndex annualSD = Indexs.annualizedStandardDeviation();
    MaxDrawdown md = Indexs.maxDrawdown(false);
    RepoAnalyser ra = new RepoAnalyser();
    CsvSaver<Result<Trader<Object>>> dailySaver = new CsvSaver<>(Files.newOutputStream(getDailyOutputFile(file)));
    dailySaver.addColumn("date", r -> r.getOrder().getDate());
    dailySaver.addColumn("price", r -> r.getOrder().getCurrentPrice());
    dailySaver.addColumn("rr", r -> r.getRepo().getReturnRate());
    dailySaver.addColumn("accumulRR", r -> accumulRR.get() - 1);
    dailySaver.addColumn("accumulTax", accumulTax);
    dailySaver.start();

    Path output = getOutputFile(file);
    uncatch(() -> Files.delete(output));
    Files.createFile(output);
    return getReader().toObservable(file)
        .lift(new ContinuousGroupOperator<>(o -> o.getDate()))
        .doOnNext(o -> System.out.printf("Calc %s data.\n", o.getLeft()))
        .concatMap(o ->
            indaySave(
                Observable.from(o.getRight()).skip(1),
                uncheck(() -> Files.newOutputStream(output, StandardOpenOption.APPEND)),
                getHook().createTraderWithParam(repo.copy())
            )
                .toObservable()
                .doOnNext(r -> ra.merge(r.getAnalysis()))
                .doOnNext(r -> md.accept(r.getRepo().getReturnRate()))
                .doOnNext(r -> accumulRR.accept(1 + r.getRepo().getReturnRate()))
                .doOnNext(r -> accumulTax.accept(r.getRepo().getPayTaxRate()))
                .doOnNext(r -> annualRR.accept(r.getRepo().getReturnRate()))
                .doOnNext(r -> annualSR.accept(r.getRepo().getReturnRate()))
                .doOnNext(r -> annualSD.accept(r.getRepo().getReturnRate()))
                .doOnNext(r -> System.out.printf("Pay tax: %.2f%%\n", 100 * r.getRepo().getPayTaxRate()))
                .doOnNext(dailySaver::row)
                .doOnCompleted(() -> System.out.println("-----------------------------------------------")))
        .count()
        .doOnCompleted(dailySaver::end)
        .doOnNext(r -> System.out.println("Summary:"))
        .doOnNext(r -> System.out.printf("Total %d trading days.\n", r))
        .doOnNext(r -> System.out.printf("Accumulated return rate: %.2f%%\n", 100 * (accumulRR.get() - 1)))
        .doOnNext(r -> System.out.printf("Max drawdown: %.2f%%\n", 100 * md.get()))
        .doOnNext(r -> System.out.printf("Return rate / Max drawdown: %.2f\n", (accumulRR.get() - 1) / md.get()))
        .doOnNext(r -> System.out.printf("Annual return rate: %.2f%%\n", 100 * annualRR.get()))
        .doOnNext(r -> System.out.printf("Annualized standard deviation : %.2f%%\n", 100 * annualSD.get()))
        .doOnNext(r -> System.out.printf("Annual sharp ratio: %.2f\n", annualSR.get()))
        .doOnNext(r -> System.out.printf("Accumulated pay tax: %.2f%%\n", 100 * accumulTax.get()))
        .doOnNext(r -> System.out.println(ra))
        .doOnCompleted(() -> System.out.println("-----------------------------------------------"));
  }

  Single<Pair<Object, Double>> paramSelectJBH(Path file, Repo repo) {
    Observable<Pair<String, List<Order>>> ob = getReader()
        .toObservable(file)
        .lift(new ContinuousGroupOperator<>(Order::getDate));
    return paramResult(
        ob,
        getHook().getParamHandler(),
        getHook().getParamSelector(),
        (o, p) -> paramToResultJBH(repo.copy(), o, p))
        .doOnSuccess(getHook()::printParamResult);
  }

  Observable<Pair<String, Pair<Object, Double>>> paramIterateJBH(Path file, Repo repo) {
    return getReader().toObservable(file)
        .lift(new ContinuousGroupOperator<String, Order>(o -> o.getName()))// split by name
        .map(pair ->
            pair.right(
                paramResult(
                    Observable.from(pair.getRight()).lift(new ContinuousGroupOperator<>(Order::getDate)),
                    getHook().getParamHandler(),
                    getHook().getParamSelector(),
                    (o, p) -> paramToResultJBH(repo.copy(), o, p)
                ).toBlocking().value()))
        .toList()
        .flatMap(Observable::from)
        .doOnNext(p -> System.out.printf("In %s, ", p.getLeft()))
        .doOnNext(p -> getHook().printParamResult(p.getRight()));
  }

  private Single<Double> paramToResultJBH(Repo repo, Observable<Pair<String, List<Order>>> o,
      Object param) {
    return o.concatMap(
        pair -> inday(
            Observable.from(pair.getRight()).skip(1),
            getHook().createTrader(repo.copy()).setParam(param))
            .last()
        )
        .lift(new IndexOperator<>(() -> getHook().getResultIndex(false)))
        .toSingle()
        .doOnSuccess(r -> getHook().printParam(Pair.of(param, r)));
  }

  /*************
   * Tian Ruan *
   *************/
  @SneakyThrows(IOException.class)
  Single<Result<Trader<Object>>> simpleTR(Path file, Repo repo) {
    return indaySave(
        getReader().toObservable(file),
        Files.newOutputStream(getOutputFile(file)),
        getHook().createTraderWithParam(repo.copy()));
  }

  Single<Pair<Object, Double>> paramTR(Path file, Repo repo) {
    return inDayParamResult(
        getReader().toObservable(file),
        getHook().getParamHandler(),
        () -> getHook().createTrader(repo.copy()),
        getHook().getResultIndex(true));
  }
}
