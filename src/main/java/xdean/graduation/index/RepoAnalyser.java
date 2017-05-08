package xdean.graduation.index;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepoAnalyser {

  HoldAnalysis longAnaly, shortAnaly;
  ReturnRateAnalysis repo, base;

  public RepoAnalyser() {
    setHoldAnalysis(new HoldAnalysis(true), new HoldAnalysis(false));
    repo = new ReturnRateAnalysis();
    base = new ReturnRateAnalysis();
  }

  public void accept(Repo r, Order o) {
    repo.accept(r.getReturnRate());
    if (o != null) {
      base.accept(o.getReturnRate());
    }
    if (r.getHold() < 0) {
      shortAnaly.accept(r);
      longAnaly.end(r);
    } else if (r.getHold() > 0) {
      longAnaly.accept(r);
      shortAnaly.end(r);
    } else {
      shortAnaly.end(r);
      longAnaly.end(r);
    }
  }

  public void merge(RepoAnalyser other) {
    setHoldAnalysis(HoldAnalysis.merge(this.longAnaly, other.longAnaly),
        HoldAnalysis.merge(this.shortAnaly, other.shortAnaly));
  }

  private void setHoldAnalysis(HoldAnalysis longAnaly, HoldAnalysis shortAnaly) {
    this.longAnaly = longAnaly;
    this.shortAnaly = shortAnaly;
  }

  private String formatHoldAnalysis() {
    return String.format("%s\n%s", longAnaly, shortAnaly);
  }

  public String toIndayString() {
    return String.format("%16s%9s%9s\n", "", "strategy", "base") +
        formatPercent("Return rate", repo.rr, base.rr) +
        formatPercent("Max return", repo.max.get(), base.max.get()) +
        formatPercent("Min return", repo.min.get(), base.min.get()) +
        formatPercent("Max drawdown", repo.md.get(), base.md.get()) +
        formatHoldAnalysis();
  }

  public String toDailyString() {
    return formatHoldAnalysis();
  }

  private String formatPercent(String name, double my, double base) {
    return String.format("%-14s: %8.2f%%%8.2f%%\n", name, my * 100, base * 100);
  }

  public static RepoAnalyser merge(RepoAnalyser a, RepoAnalyser b) {
    RepoAnalyser merge = new RepoAnalyser();
    merge.setHoldAnalysis(HoldAnalysis.merge(a.longAnaly, b.longAnaly), HoldAnalysis.merge(a.shortAnaly, b.shortAnaly));
    return merge;
  }
}