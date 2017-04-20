package xdean.graduation.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import xdean.graduation.index.RepoAnalyser;

@AllArgsConstructor
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Result<T> {
  Order order;
  Repo repo;
  RepoAnalyser analysis;
  T trader;
}
