package com.tinqinacademy.authentication.core.converters.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

@Slf4j
public abstract class BaseConverter<S, T> implements Converter<S, T> {

  @Override
  public T convert(S source) {
    log.info("Start convert source: {}", source);
    if (source == null) {
      return null;
    }
    T result = doConvert(source);
    log.info("End convert target: {}", result);
    return result;
  }

  protected abstract T doConvert(S source);
}
