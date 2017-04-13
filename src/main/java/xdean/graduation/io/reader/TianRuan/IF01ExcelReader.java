package xdean.graduation.io.reader.TianRuan;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import lombok.SneakyThrows;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import rx.Observable;
import xdean.graduation.io.reader.BaseReader;
import xdean.graduation.model.Order;
import xdean.jex.extra.ThreadSafeDateFormat;
import xdean.jex.util.TimeUtil;
import xdean.jex.util.task.TaskUtil;

import com.google.common.collect.Lists;

public class IF01ExcelReader extends BaseReader<Path, Order> {
  private static final ThreadSafeDateFormat DATE_FORMAT =
      new ThreadSafeDateFormat(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

  @Override
  protected Observable<Order> convert(Observable<Path> o) {
    return o.map(p -> TimeUtil.timeThenPrint(() -> TaskUtil.uncheck(() -> WorkbookFactory.create(p.toFile()).getSheetAt(0)), "read excel in %dms\n"))
        .flatMap(sheet -> Observable.from(Lists.newArrayList(sheet.rowIterator())))
        .skip(2)
        .map(this::rowToOrder);
  }

  //TODO: name
  @SneakyThrows(ParseException.class)
  private Order rowToOrder(Row row) {
    long time = DATE_FORMAT.parse(row.getCell(2).getStringCellValue()).getTime();
    double currentPrice = row.getCell(3).getNumericCellValue();
    double volume = row.getCell(4).getNumericCellValue();
    double amount = row.getCell(5).getNumericCellValue();
    double averagePrice = volume == 0 ? currentPrice : amount / volume / 300;
    double buyPrice = row.getCell(10).getNumericCellValue();
    double sellPrice = row.getCell(15).getNumericCellValue();
    double lastClosePrice = row.getCell(45).getNumericCellValue();
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
