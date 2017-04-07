package xdean.graduation.io.writer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;

import xdean.jex.extra.Pair;

import com.google.common.collect.Lists;

public class CsvSaver<D> implements DataWriter<D> {

  List<Pair<String, Function<D, ?>>> columns = Lists.newArrayList();
  boolean started;
  boolean closed;
  PrintStream ps;

  public CsvSaver(OutputStream output) {
    ps = new PrintStream(output);
  }

  public CsvSaver(PrintStream output) {
    ps = output;
  }

  @Override
  public boolean addColumn(String columnName, Function<D, ?> columnFactory) {
    if (started) {
      return false;
    }
    columns.add(Pair.of(columnName, columnFactory));
    return true;
  }

  @Override
  public void start() {
    if (started) {
      return;
    }
    started = true;
    ps.println(columns.stream()
        .map(Pair::getLeft)
        .map(s -> s.replace(',', ';'))
        .reduce((s1, s2) -> s1 + "," + s2).get());
  }

  @Override
  public void row(D d) {
    if (started == false) {
      return;
    }
    ps.println(columns.stream()
        .map(Pair::getRight)
        .map(f -> f.apply(d))
        .map(s -> s.toString())
        .map(s -> s.replace(',', ';'))
        .reduce((s1, s2) -> s1 + "," + s2).get());
  }

  @Override
  public void end() {
    if (started == false || closed) {
      return;
    }
    closed = true;
    ps.flush();
    ps.close();
  }
}
