package org.opencastproject.workflow.handler.extron.smp351.validator.functional;

public interface Effect<T> {
  void apply(T t);
}