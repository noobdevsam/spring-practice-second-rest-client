package com.example.springpracticesecondrestclient.client;

import com.example.springpracticesecondrestclient.model.BeerDTO;
import com.example.springpracticesecondrestclient.model.BeerStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerClientImpl implements BeerClient {

    public static final String GET_BEER_PATH = "/api/v1/beer";
    public static final String GET_BEER_BY_ID_PATH = "/api/v1/beer/{beerId}";

    private final RestClient.Builder restClientBuilder;

    @Override
    public Page<BeerDTO> listBeers() {
        return null;
    }

    @Override
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        return null;
    }

    @Override
    public BeerDTO getBeerById(UUID beerId) {
        var restClient = restClientBuilder.build();

        return restClient.get()
                .uri(
                        uriBuilder -> uriBuilder.path(GET_BEER_BY_ID_PATH).build(beerId)
                )
                .retrieve()
                .body(BeerDTO.class);
    }

    @Override
    public BeerDTO createBeer(BeerDTO newBeerDTO) {
        var restClient = restClientBuilder.build();

        var location = restClient.post()
                .uri(uriBuilder -> uriBuilder.path(GET_BEER_PATH).build())
                .body(newBeerDTO)
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation();

        assert location != null;

        return restClient.get()
                .uri(location.getPath())
                .retrieve()
                .body(BeerDTO.class);
    }

    @Override
    public BeerDTO updateBeer(BeerDTO beerDTO) {
        var restClient = restClientBuilder.build();

        restClient.put()
                .uri(
                        uriBuilder -> uriBuilder.path(GET_BEER_BY_ID_PATH).build(beerDTO.getId())
                )
                .body(beerDTO)
                .retrieve()
                .toBodilessEntity();

        return getBeerById(beerDTO.getId());
    }

    @Override
    public void deleteBeerById(UUID beerId) {

    }
}
