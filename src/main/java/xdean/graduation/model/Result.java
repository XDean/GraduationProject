package xdean.graduation.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import xdean.graduation.index.RepoAnalysis;
import xdean.jex.extra.Pair;

@AllArgsConstructor
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Result<T> {
  Order order;
  Repo repo;
  Pair<RepoAnalysis, RepoAnalysis> analysis;
  T trader;
}
