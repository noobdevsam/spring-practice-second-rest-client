package com.example.springpracticesecondrestclient.client;

import com.example.springpracticesecondrestclient.config.OauthClientInterceptor;
import com.example.springpracticesecondrestclient.config.RestTemplateConfig;
import com.example.springpracticesecondrestclient.model.BeerDTO;
import com.example.springpracticesecondrestclient.model.BeerDTOPageImpl;
import com.example.springpracticesecondrestclient.model.BeerStyle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest
@Import(RestTemplateConfig.class)
class BeerClientMockTest {

    public static final RequestMatcher BASIC_AUTHORIZATION_HEADER = header("Authorization", "Bearer test");
    static final String base_url = "http://localhost:8080";
    BeerClient beerClient;

    MockRestServiceServer mockRestServiceServer;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Autowired
    RestClient.Builder restClientBuilder;

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    BeerDTO dto;
    String dtoJson;

    @MockitoBean
    OAuth2AuthorizedClientManager manager;
    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(
            new MockServerRestTemplateCustomizer()
    );

    @BeforeEach
    void setUp() throws JsonProcessingException {

        var clientRegistration = clientRegistrationRepository.findByRegistrationId("springauth");
        var token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test",
                Instant.MIN,
                Instant.MAX
        );

        when(
                manager.authorize(any())
        ).thenReturn(
                new OAuth2AuthorizedClient(clientRegistration, "test", token)
        );

        var restTemplate = restTemplateBuilderConfigured.build();
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();

        when(
                mockRestTemplateBuilder.build()
        ).thenReturn(
                restTemplate
        );

        beerClient = new BeerClientImpl(RestClient.builder(mockRestTemplateBuilder.build()));

        dto = getBeerDTO();
        dtoJson = objectMapper.writeValueAsString(dto);

    }

    @Test
    void test_list_beers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());

        mockRestServiceServer
                .expect(method(HttpMethod.GET))
                .andExpect(
                        requestTo(base_url + BeerClientImpl.GET_BEER_PATH)
                ).andRespond(
                        withSuccess(payload, MediaType.APPLICATION_JSON)
                );

        var dtos = beerClient.listBeers();

        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }

    @Test
    void test_get_by_id() {

        mockGetOperation();

        var responseDto = beerClient.getBeerById(dto.getId());
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void test_create_beer() {
        var uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH)
                .build(dto.getId());

        mockRestServiceServer
                .expect(method(HttpMethod.POST))
                .andExpect(
                        requestTo(base_url + BeerClientImpl.GET_BEER_PATH)
                ).andExpect(
                        BASIC_AUTHORIZATION_HEADER
                ).andRespond(
                        withAccepted().location(uri)
                );

        mockGetOperation();

        var responseDto = beerClient.createBeer(dto);
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void test_update_beer() {
        mockRestServiceServer
                .expect(method(HttpMethod.PUT))
                .andExpect(
                        requestToUriTemplate(base_url + BeerClientImpl.GET_BEER_BY_ID_PATH,
                                dto.getId())
                ).andExpect(
                        BASIC_AUTHORIZATION_HEADER
                ).andRespond(
                        withNoContent()
                );

        mockGetOperation();

        var responseDto = beerClient.updateBeer(dto);
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void test_delete_beer() {
        mockRestServiceServer
                .expect(method(HttpMethod.DELETE))
                .andExpect(
                        requestToUriTemplate(base_url + BeerClientImpl.GET_BEER_BY_ID_PATH,
                                dto.getId())
                ).andExpect(
                        BASIC_AUTHORIZATION_HEADER
                ).andRespond(
                        withNoContent()
                );

        beerClient.deleteBeerById(dto.getId());

        mockRestServiceServer.verify(); // Verify that the DELETE request was made
    }

    @Test
    void test_delete_beer_not_found() {
        mockRestServiceServer
                .expect(method(HttpMethod.DELETE))
                .andExpect(
                        requestToUriTemplate(base_url + BeerClientImpl.GET_BEER_BY_ID_PATH,
                                dto.getId())
                ).andExpect(
                        BASIC_AUTHORIZATION_HEADER
                ).andRespond(
                        withResourceNotFound()
                );

        assertThrows(
                HttpClientErrorException.class, () -> beerClient.deleteBeerById(dto.getId())
        );

        mockRestServiceServer.verify(); // Verify that the DELETE request was made
    }

    @Test
    void test_list_beers_query_param() throws JsonProcessingException {
        var response = objectMapper.writeValueAsString(getPage());
        var uri = UriComponentsBuilder
                .fromUriString(base_url + BeerClientImpl.GET_BEER_PATH)
                .queryParam("beerName", "ALE")
                .build().toUri();

        mockRestServiceServer
                .expect(method(HttpMethod.GET))
                .andExpect(
                        requestTo(uri)
                ).andExpect(
                        BASIC_AUTHORIZATION_HEADER
                ).andExpect(
                        queryParam("beerName", "ALE")
                ).andRespond(
                        withSuccess(response, MediaType.APPLICATION_JSON)
                );

        var response_page = beerClient
                .listBeers("ALE", null, null, null, null);

        assertThat(response_page.getContent().size()).isEqualTo(1);
    }

    private void mockGetOperation() {
        mockRestServiceServer
                .expect(method(HttpMethod.GET))
                .andExpect(
                        requestToUriTemplate(base_url + BeerClientImpl.GET_BEER_BY_ID_PATH,
                                dto.getId())
                ).andExpect(
                        BASIC_AUTHORIZATION_HEADER
                ).andRespond(
                        withSuccess(dtoJson, MediaType.APPLICATION_JSON)
                );
    }

    BeerDTOPageImpl getPage() {
        return new BeerDTOPageImpl(Collections.singletonList(getBeerDTO()), 1, 25, 1);
    }

    BeerDTO getBeerDTO() {
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .beerName("Nerd Beer")
                .beerStyle(BeerStyle.IPA)
                .upc("656554")
                .price(new BigDecimal("456.23"))
                .quantityOnHand(1252)
                .build();
    }

    @TestConfiguration
    public static class TestConfig {

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(
                    ClientRegistration.withRegistrationId("springauth")
                            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                            .clientId("test")
                            .tokenUri("test")
                            .build()
            );
        }

        @Bean
        OAuth2AuthorizedClientService auth2AuthorizedClientService(
                ClientRegistrationRepository clientRegistrationRepository
        ) {
            return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        }

        @Bean
        OauthClientInterceptor oauthClientInterceptor(
                OAuth2AuthorizedClientManager manager,
                ClientRegistrationRepository clientRegistrationRepository
        ) {
            return new OauthClientInterceptor(manager, clientRegistrationRepository);
        }
    }
}