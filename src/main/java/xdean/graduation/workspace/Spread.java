package xdean.graduation.workspace;

import static xdean.graduation.workspace.Util.getReader;

import java.nio.file.Path;
import java.nio.file.Paths;

import xdean.graduation.handler.IndexOperator;
import xdean.graduation.index.base.Indexs;

public class Spread {
  public static void main(String[] args) {
    Path file = Paths.get("data", "rb170510.jbh.bsv");
    getReader().read(file)
        .map(o -> o.getSellPrice() - o.getBuyPrice())
        .filter(d -> Math.abs(d) < 100)
        .lift(IndexOperator.create(() -> Indexs.average()))
        .subscribe(System.out::println);
  }
}
