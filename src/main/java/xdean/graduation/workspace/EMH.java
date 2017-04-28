package xdean.graduation.workspace;

import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.experimental.UtilityClass;
import rx.Observable;
import xdean.graduation.handler.TimeOperator;
import xdean.graduation.index.RunsTest;
import xdean.jex.extra.rx.op.ContinuousGroupOperator;

@UtilityClass
public class EMH {
  public void main(String[] args) {
    Path file = Paths.get("data", "IF1701.jbh.bsv");
    Util.getReader()
        .read(file)
        .takeUntil(o -> o.getDate().compareTo("20170201") > 0)
        .lift(new ContinuousGroupOperator<>(o -> null))
        .forEach(pair -> {
          RunsTest runsTest = new RunsTest();
          Observable.from(pair.getRight())
              .lift(new TimeOperator(10 * 1000, 1000 * 1000))
              // .doOnNext(o->System.out.println(o.getTime()))
              .doOnSubscribe(() -> System.out.println(pair.getLeft() + ":"))
              .doOnNext(r -> runsTest.accept(r.getCurrentPrice()))
              .doOnCompleted(() -> System.out.println(runsTest))
              .subscribe();
        });
  }
}
