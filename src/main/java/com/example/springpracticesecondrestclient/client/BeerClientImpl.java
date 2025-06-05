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
        return null;
    }

    @Override
    public BeerDTO createBeer(BeerDTO newBeerDTO) {
        return null;
    }

    @Override
    public BeerDTO updateBeer(BeerDTO beerDTO) {
        return null;
    }

    @Override
    public void deleteBeerById(UUID beerId) {

    }
}
