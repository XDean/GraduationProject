package xdean.graduation.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import xdean.jex.extra.ThreadSafeDateFormat;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class Order {

  private static final ThreadSafeDateFormat DATE_FORMAT = new ThreadSafeDateFormat(
      () -> new SimpleDateFormat("yyyyMMdd"));
  private static final ThreadSafeDateFormat TIME_FORMAT = new ThreadSafeDateFormat(
      () -> new SimpleDateFormat("HH:mm:ss"));

  long timeStamp;
  String name;
  double lastClosePrice;
  double lastClearPrice;
  double currentPrice;
  double averagePrice; // average price between the order and the last
  double volume;
  double sellPrice;
  double buyPrice;
  String date;
  String time;

  public Order(long timeStamp, String name, double lastClosePrice, double lastClearPrice, double currentPrice, double averagePrice, double volume,
      double sellPrice,      double buyPrice,
      String date, String time) {
    this(timeStamp, name, lastClosePrice, lastClearPrice, currentPrice, averagePrice, volume, sellPrice, buyPrice);
  }

  public Order(long timeStamp, String name, double lastClosePrice, double lastClearPrice, double currentPrice, double averagePrice, double volume,
      double sellPrice,
      double buyPrice) {
    super();
    this.timeStamp = timeStamp;
    this.name = name;
    this.lastClosePrice = lastClosePrice;
    this.lastClearPrice = lastClearPrice;
    this.currentPrice = currentPrice;
    this.averagePrice = averagePrice;
    this.volume = volume;
    this.sellPrice = sellPrice;
    this.buyPrice = buyPrice;
    this.date = DATE_FORMAT.format(new Date(timeStamp));
    this.time = TIME_FORMAT.format(new Date(timeStamp));
  }

  /**
   * Look like 19700101
   */
  public String getDate() {
    return date;
  }

  public static Order merge(Order a, Order b) {
    double volume = a.getVolume() + b.getVolume();
    return b.toBuilder()
        .volume(volume)
        .averagePrice(
            volume == 0 ? b.getCurrentPrice() :
                (a.getAveragePrice() * a.getVolume() +
                    b.getAveragePrice() * b.getVolume())
                    / volume)
        .build();
  }
}
