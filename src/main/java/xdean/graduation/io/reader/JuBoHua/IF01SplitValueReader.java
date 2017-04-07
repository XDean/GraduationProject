package xdean.graduation.io.reader.JuBoHua;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import rx.Observable;
import xdean.graduation.io.reader.BaseReader;
import xdean.graduation.model.Order;
import xdean.graduation.workspace.Util;
import xdean.jex.extra.ThreadSafeDateFormat;
import xdean.jex.extra.Wrapper;

@RequiredArgsConstructor
public class IF01SplitValueReader extends BaseReader<Path, Order> {
  private static final ThreadSafeDateFormat DATE_FORMAT =
      new ThreadSafeDateFormat(() -> new SimpleDateFormat("yyyyMMddHH:mm:ss"));

  final String splitRegex;
  Wrapper<Double> oldTotalVolume = Wrapper.of(0d);
  Wrapper<Double> oldTotalAmount = Wrapper.of(0d);

  @Override
  protected Observable<Order> convert(Observable<Path> o) {
    return o.flatMap(Util::lines)
        .map(this::rowToOrder);
  }

  @SneakyThrows(ParseException.class)
  private Order rowToOrder(String row) {
    String[] split = row.split(splitRegex);
    long time = DATE_FORMAT.parse(split[0] + split[18]).getTime();
    String name = split[1];
    double currentPrice = Double.valueOf(split[2]);
    double lastClosePrice = Double.valueOf(split[4]);
    double volume = interpolation(oldTotalVolume, Double.valueOf(split[9]));
    double amount = interpolation(oldTotalAmount, Double.valueOf(split[10]));
    double averagePrice = volume == 0 ? currentPrice : amount / volume / 300;
    double buyPrice = Double.valueOf(split[20]);
    double sellPrice = Double.valueOf(split[30]);
    return Order.builder()
        .time(time)
        .name(name)
        .lastClosePrice(lastClosePrice)
        .currentPrice(currentPrice)
        .averagePrice(averagePrice)
        .volume(volume)
        .buyPrice(buyPrice)
        .sellPrice(sellPrice)
        .build();
  }

  private double interpolation(Wrapper<Double> wrap, double newValue) {
    double result = newValue - wrap.get();
    wrap.set(newValue);
    return result;
  }
}
