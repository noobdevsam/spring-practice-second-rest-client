package com.example.springpracticesecondrestclient.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static java.util.Objects.isNull;

// It is used to intercept HTTP requests made by a RestTemplate
// and add an OAuth2 Bearer token to the Authorization header of the request.
// -
// The class ensures that every HTTP request made by the RestTemplate includes a valid OAuth2 Bearer token for
// authentication. It uses the OAuth2AuthorizedClientManager to handle the token retrieval and management process.
@Component
public class OauthClientInterceptor implements ClientHttpRequestInterceptor {

    // OAuth2AuthorizedClientManager: Manages the authorization of OAuth2 clients.
    private final OAuth2AuthorizedClientManager manager;

    // ClientRegistration: Contains the configuration for the OAuth2 client (e.g., client ID, client secret, etc.).
    private final ClientRegistration clientRegistration;

    // The constructor initializes the manager, principal, and clientRegistration fields,
    // which are required for authorizing the client and obtaining an access token.
    public OauthClientInterceptor(
            OAuth2AuthorizedClientManager manager,
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        this.manager = manager;

        // Authentication: Represents the principal (user or client) making the request.
        // The clientRegistration is obtained from the ClientRegistrationRepository using the registration ID "springauth".
        this.clientRegistration = clientRegistrationRepository.findByRegistrationId("springauth");
    }


    // This method is called for every HTTP request intercepted by the RestTemplate.
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {

        // It creates an OAuth2AuthorizeRequest using the clientRegistration and a custom Authentication object
        // (created by the createPrincipal method).
        var authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistration.getRegistrationId())
                .principal(clientRegistration.getClientId())
                .build();

        // The manager.authorize method is used to obtain an OAuth2AuthorizedClient, which contains the access token.
        var authorizedClient = manager.authorize(authorizeRequest);

        if (isNull(authorizedClient)) {
            throw new IllegalStateException("Missing credentials");
        }

        // The access token is added to the Authorization header of the HTTP request in the format Bearer <token>.
        request.getHeaders().add(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + authorizedClient.getAccessToken().getTokenValue()
        );

        // Finally, the request is executed using the execution.execute method
        return execution.execute(request, body);
    }
}
