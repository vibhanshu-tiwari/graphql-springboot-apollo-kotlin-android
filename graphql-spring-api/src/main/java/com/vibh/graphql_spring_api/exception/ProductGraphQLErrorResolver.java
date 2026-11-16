package com.vibh.graphql_spring_api.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ProductGraphQLErrorResolver implements DataFetcherExceptionResolver {

    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable exception, DataFetchingEnvironment environment) {

        if(exception instanceof ProductNotFoundException) {
            GraphQLError error = GraphqlErrorBuilder.newError(environment)
                    .message(exception.getMessage())
                    .errorType(ErrorType.NOT_FOUND)
                    .build();

            return Mono.just(List.of(error));
        }
        return Mono.empty();
    }
}
