package org.opencastproject.workflow.handler.extron.smp351.validator.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ListUtilities {

  /**
   *
   *
   *
   * @param list
   * @param f
   * @return
   * @param <A>
   * @param <B>
   */
  public static <A,B> Result<List<B>> traverse(List<A> list, Function<A, B> f) {
    Result<List<A>> rList = Result.of(list);
    return rList.flatMap(xs ->  {
      List<B> bs = new LinkedList<>();
      for (A x: xs) {
        try {
          bs.add(f.apply(x));
        } catch (Exception e) {
          return Result.failure(e);
        }
      }
      return Result.success(bs);
    });
  }

  public static <A,B> List<Result<B>> map(List<A> list, Function<A, B> f) {
      List<Result<B>> bs = new LinkedList<>();
      for (A a : list) {
        try {
          bs.add(Result.of(f.apply(a)));
        } catch (Exception e) {
          bs.add(Result.failure(e));
        }
      }
      return bs;
  }

  /* Composing List and Result - Included Successes Only */
  public static <A> List<A> flattenResult(List<Result<A>> rList) {
    List<A> newList = new ArrayList<>();
    for (Result<A> a: rList) {
        if (a.isSuccess()) newList.add(a.successValue());
    }
    return newList;
  }

  public static <A, B> B foldLeft(List<A> list, B identity, Function<B, Function<A, B>> f) {
    Objects.requireNonNull(list, "The list may not be null!");
    B accumulator = identity;
    for (A a: list) {
      accumulator = f.apply(accumulator).apply(a);
    }
    return accumulator;
  }

  public static <A> Result<A> head(List<A> list) {
    try {
      return Result.success(list.get(0));
    } catch (Exception e) {
      return Result.failure(e);
    }
  }

  public static <A> void forEach(List<A> list, Consumer<A> effect) {
    for (A elem : list) {
      effect.accept(elem);
    }
  }

  public static <A> List<A> append(List<A> list, A value) {
    List<A> as = List.copyOf(list);
    as.add(value);
    return list(as);
  }


  public static <T> List<T> list() {
    return Collections.emptyList();
  }

  public static <T> List<T> list(T t) {
    return Collections.singletonList(t);
  }

  public static <T> List<T> list(List<T> ts) {
    return Collections.unmodifiableList(new ArrayList<>(ts));
  }
  public static <T> List<T> list(T... t) {
    return Collections.unmodifiableList(Arrays.asList(Arrays.copyOf(t, t.length)));
  }
}
