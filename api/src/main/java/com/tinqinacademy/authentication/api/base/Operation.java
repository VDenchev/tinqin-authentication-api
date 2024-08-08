package com.tinqinacademy.authentication.api.base;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import io.vavr.control.Either;

public interface Operation <I extends OperationInput, O extends OperationOutput> {

  Either<? extends ErrorOutput, O> process(I input);
}
