package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.getuserbyphoneno.input.SearchUsersInput;
import com.tinqinacademy.authentication.api.operations.getuserbyphoneno.operation.SearchUsersOperation;
import com.tinqinacademy.authentication.api.operations.getuserbyphoneno.output.SearchUsersOutput;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.repositories.UserRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.vavr.API.Match;

@Service
@Slf4j
public class SearchUsersOperationProcessor extends BaseOperationProcessor implements SearchUsersOperation {

  private final UserRepository userRepository;

  public SearchUsersOperationProcessor(ConversionService conversionService, Validator validator, UserRepository userRepository) {
    super(conversionService, validator);
    this.userRepository = userRepository;
  }

  @Override
  public Either<? extends ErrorOutput, SearchUsersOutput> process(SearchUsersInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start search users input: {}", input);

                  String phoneNo = StringUtils.defaultString(input.getPhoneNo());

                  List<User> users = userRepository.findAllByPhoneNumberContaining(phoneNo);

                  SearchUsersOutput output = createOutput(users);
                  log.info("End search users output: {}", output);
                  return output;
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    defaultCase(t)
                ))
        );
  }

  private SearchUsersOutput createOutput(List<User> users) {
    List<String> userIds = users.stream()
        .map(u -> u.getId().toString())
        .toList();
    SearchUsersOutput output = SearchUsersOutput.builder()
        .userIds(userIds)
        .build();
    return output;
  }
}
