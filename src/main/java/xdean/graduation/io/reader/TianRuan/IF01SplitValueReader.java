package xdean.graduation.io.reader.TianRuan;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import rx.Observable;
import xdean.graduation.io.reader.BaseReader;
import xdean.graduation.model.Order;
import xdean.graduation.workspace.Util;
import xdean.jex.extra.ThreadSafeDateFormat;

@AllArgsConstructor
public class IF01SplitValueReader extends BaseReader<Path, Order> {
  private static final ThreadSafeDateFormat DATE_FORMAT =
      new ThreadSafeDateFormat(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

  String splitRegex;

  @Override
  protected Observable<Order> convert(Observable<Path> o) {
    return o.flatMap(Util::lines)
        .skip(2)
        .map(this::rowToOrder);
  }

  // TODO name
  @SneakyThrows(ParseException.class)
  private Order rowToOrder(String row) {
    String[] split = row.split(splitRegex);
    long time = DATE_FORMAT.parse(split[2]).getTime();
    double currentPrice = Double.valueOf(split[3]);
    double volume = Double.valueOf(split[4]);
    double amount = Double.valueOf(split[5]);
    double averagePrice = volume == 0 ? currentPrice : amount / volume / 300;
    double buyPrice = Double.valueOf(split[10]);
    double sellPrice = Double.valueOf(split[15]);
    double lastClosePrice = Double.valueOf(split[45]);
    return Order.builder()
        .timeStamp(time)
        .lastClosePrice(lastClosePrice)
        .currentPrice(currentPrice)
        .averagePrice(averagePrice)
        .volume(volume)
        .buyPrice(buyPrice)
        .sellPrice(sellPrice)
        .build();
  }
}
