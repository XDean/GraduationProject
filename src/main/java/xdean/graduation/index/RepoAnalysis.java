package xdean.graduation.index;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import xdean.graduation.model.Repo;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepoAnalysis {
  @Getter
  int count;
  @Getter
  double profit;

  final boolean isLong;
  boolean end = true;
  int winCount;
  double lastPrice;
  double startProfit;
  double holdLength;

  public RepoAnalysis(boolean isLong) {
    this.isLong = isLong;
  }

  void accept(Repo t) {
    if (end) {
      end = false;
      count++;
      startProfit = profit;
    } else {
      profit += isLong ? (t.getPrice() - lastPrice) : (lastPrice - t.getPrice());
      holdLength++;
    }
    lastPrice = t.getPrice();
  }

  void end() {
    if (end == false) {
      end = true;
      if (profit > startProfit) {
        winCount++;
      }
    }
  }

  public double getWinPercent() {
    return count == 0 ? 0 : ((double) (winCount + (end ? 0 : isNowWin()))) / count;
  }

  private int isNowWin() {
    return profit > startProfit ? 1 : 0;
  }

  public double getHoldLength() {
    return count == 0 ? 0 : holdLength / count;
  }

  public static RepoAnalysis merge(RepoAnalysis a, RepoAnalysis b) {
    if (a.isLong ^ b.isLong) {
      throw new IllegalArgumentException("Cannot merge long and short.");
    }
    RepoAnalysis merge = new RepoAnalysis(a.isLong);
    merge.count = a.count + b.count;
    merge.end = a.end && b.end;
    merge.holdLength = a.holdLength + b.holdLength;
    merge.lastPrice = b.end ? a.lastPrice : b.lastPrice;
    merge.profit = a.profit + b.profit;
    merge.startProfit = a.startProfit + b.startProfit;
    merge.winCount = a.winCount + b.winCount;
    return merge;
  }

  @Override
  public String toString() {
    return String.format(
        "%s: %d time, profit %.2f point, win percent %.2f%%, average hold %.0f term.",
        isLong ? "Long" : "Short", getCount(), getProfit(), getWinPercent() * 100, getHoldLength());
  }
}