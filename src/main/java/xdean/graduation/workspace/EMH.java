package xdean.graduation.workspace;

import java.nio.file.Path;
import java.nio.file.Paths;

import rx.Observable;
import lombok.experimental.UtilityClass;
import xdean.graduation.handler.TimeOperator;
import xdean.graduation.index.RunsTest;
import xdean.graduation.model.Order;
import xdean.jex.extra.rx.op.ContinuousGroupOperator;

@UtilityClass
public class EMH {
  public void main(String[] args) {
    Path file = Paths.get("data", "rb170510.jbh.bsv");
    Util.getReader()
        .read(file)
        .lift(new ContinuousGroupOperator<>(Order::getName))
        .forEach(pair -> {
          RunsTest runsTest = new RunsTest();
          Observable.from(pair.getRight())
              .lift(new TimeOperator(100 * 1000, 1000 * 1000))
//              .doOnNext(o->System.out.println(o.getTime()))
              .doOnSubscribe(() -> System.out.println(pair.getLeft() + ":"))
              .doOnNext(r -> runsTest.accept(r.getCurrentPrice()))
              .doOnCompleted(() -> System.out.println(runsTest))
              .subscribe();
        });
  }
}
