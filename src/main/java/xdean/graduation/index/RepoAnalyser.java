package xdean.graduation.index;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import xdean.graduation.index.base.Index;
import xdean.graduation.model.Repo;
import xdean.jex.extra.Pair;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepoAnalyser implements Index<Repo, RepoAnalyser> {

  RepoAnalysis longAnaly, shortAnaly;

  public RepoAnalyser() {
    set(new RepoAnalysis(true), new RepoAnalysis(false));
  }

  @Override
  public void accept(Repo t) {
    if (t.getHold() < 0) {
      shortAnaly.accept(t);
      longAnaly.end();
    } else if (t.getHold() > 0) {
      longAnaly.accept(t);
      shortAnaly.end();
    } else {
      shortAnaly.end();
      longAnaly.end();
    }
  }

  @Override
  public RepoAnalyser get() {
    return this;
  }

  public void merge(RepoAnalyser other) {
    set(RepoAnalysis.merge(this.longAnaly, other.longAnaly),
        RepoAnalysis.merge(this.shortAnaly, other.shortAnaly));
  }

  public void merge(Pair<RepoAnalysis, RepoAnalysis> pair) {
    set(RepoAnalysis.merge(this.longAnaly, pair.getLeft()),
        RepoAnalysis.merge(this.shortAnaly, pair.getRight()));
  }

  private void set(RepoAnalysis longAnaly, RepoAnalysis shortAnaly) {
    this.longAnaly = longAnaly;
    this.shortAnaly = shortAnaly;
  }

  @Override
  public String toString() {
    return toString(longAnaly, shortAnaly);
  }

  public static String toString(RepoAnalysis longRa, RepoAnalysis shortRa) {
    return String.format("%s\n%s", longRa, shortRa);
  }

  public static RepoAnalyser merge(RepoAnalyser a, RepoAnalyser b) {
    RepoAnalyser merge = new RepoAnalyser();
    merge.set(RepoAnalysis.merge(a.longAnaly, b.longAnaly), RepoAnalysis.merge(a.shortAnaly, b.shortAnaly));
    return merge;
  }
}