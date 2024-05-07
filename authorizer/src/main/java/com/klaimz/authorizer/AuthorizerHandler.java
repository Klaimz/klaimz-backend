package com.klaimz.authorizer;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.AppSyncLambdaAuthorizerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.function.aws.MicronautRequestHandler;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.HashMap;


public class AuthorizerHandler extends MicronautRequestHandler<APIGatewayV2CustomAuthorizerEvent, AppSyncLambdaAuthorizerResponse> {

    @Inject
    private CoreLambdaClient coreLambdaClient;

    @Inject
    private ObjectMapper objectMapper;

    /**
     * Executes the authorization process for the incoming APIGatewayV2CustomAuthorizerEvent.
     * <p>
     * This method takes an APIGatewayV2CustomAuthorizerEvent as input, checks if it's a warmup request,
     * and if so, returns a success response. If it's not a warmup request, it converts the input into an
     * APIGatewayV2HTTPEvent and sends it to the coreLambdaClient for token validation.
     * <p>
     * If the token validation is successful (response status code is 200), it returns an authorization success response.
     * If the token validation fails, it returns an authorization failure response.
     *
     * @param input the APIGatewayV2CustomAuthorizerEvent to be authorized
     * @return the AppSyncLambdaAuthorizerResponse indicating whether the authorization was successful or not
     * @throws RuntimeException if an I/O error occurs during the conversion of the APIGatewayV2CustomAuthorizerEvent
     */
    @Override
    public AppSyncLambdaAuthorizerResponse  execute(APIGatewayV2CustomAuthorizerEvent input) {
        try {

            if (input.getRawPath().contains("/warmup")) {
                // This is a warmup request, so we just return a success response
                return AppSyncLambdaAuthorizerResponse.builder()
                        .withIsAuthorized(true)
                        .build();
            }

            var httpEvent = convertEvent(input);
            var response = coreLambdaClient.validateToken(httpEvent);

            if (response.getStatusCode() == 200) {
                System.out.println("Token is valid");
                return AppSyncLambdaAuthorizerResponse.builder()
                        .withIsAuthorized(true)
                        .build();
            } else {
                System.out.println("Token is invalid");
                return AppSyncLambdaAuthorizerResponse.builder()
                        .withIsAuthorized(false)
                        .build();
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Converts an APIGatewayV2CustomAuthorizerEvent into an APIGatewayV2HTTPEvent.
     * This method takes an APIGatewayV2CustomAuthorizerEvent as input, serializes it into JSON,
     * and then deserializes it into an APIGatewayV2HTTPEvent. It also sets the route key, raw path,
     * and HTTP method to "GET /user/me".
     *
     * @param apiGatewayEvent the APIGatewayV2CustomAuthorizerEvent to be converted
     * @return the converted APIGatewayV2HTTPEvent
     * @throws IOException if an I/O error occurs during serialization or deserialization
     */
    private APIGatewayV2HTTPEvent convertEvent(APIGatewayV2CustomAuthorizerEvent apiGatewayEvent) throws IOException {
        apiGatewayEvent.getRequestContext().setTime("01/Jan/2000:11:59:59 +0530");
        // Set all properties of APIGatewayV2CustomAuthorizerEvent to null which don't exist in APIGatewayV2HTTPEvent
        apiGatewayEvent.setType(null);
        apiGatewayEvent.setRouteArn(null);
        apiGatewayEvent.setIdentitySource(null);

        // For RequestContext
        APIGatewayV2CustomAuthorizerEvent.RequestContext requestContext = apiGatewayEvent.getRequestContext();
        if (requestContext != null) {
            requestContext.setRouteKey(null);
            requestContext.setDomainName(null);
            requestContext.setDomainPrefix(null);
        }

        var inputJson = objectMapper.writeValueAsString(apiGatewayEvent);
        System.out.println(inputJson);
        var eventMap = objectMapper.readValue(inputJson, HashMap.class);

        ((HashMap) eventMap.get("requestContext"))
                .remove("timeEpoch"); // fix for timeEpoch

        var event = objectMapper.convertValue(eventMap, APIGatewayV2HTTPEvent.class);

        event.setRouteKey("GET /user/me");
        event.setRawPath("/user/me");
        var context = event.getRequestContext();
        context.setRouteKey("GET /user/me");
        var httpApi = context.getHttp();
        httpApi.setPath("/user/me");
        httpApi.setMethod("GET");

        return event;
    }
}
