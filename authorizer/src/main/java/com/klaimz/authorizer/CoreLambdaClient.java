package com.klaimz.authorizer;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.function.client.FunctionClient;
import jakarta.inject.Named;

@FunctionClient
public interface CoreLambdaClient {

    @Named("core")
    APIGatewayV2HTTPResponse validateToken(APIGatewayV2HTTPEvent token);

}
