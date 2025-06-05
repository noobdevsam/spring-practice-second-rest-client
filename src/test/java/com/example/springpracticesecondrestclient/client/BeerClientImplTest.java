package com.example.springpracticesecondrestclient.client;

import com.example.springpracticesecondrestclient.model.BeerDTO;
import com.example.springpracticesecondrestclient.model.BeerStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClientImpl beerClient;

    @Test
    void listBeers() {

        beerClient.listBeers("ALE", null, null, null, null);

    }

    @Test
    void listBeersNoBeerName() {

        beerClient.listBeers(null, null, null, null, null);

    }

    @Test
    void testGetById() {

        Page<BeerDTO> beerDTOs = beerClient.listBeers();
        BeerDTO beerDTO = beerDTOs.getContent().getFirst();

        BeerDTO beerDT_O1 = beerClient.getBeerById(beerDTO.getId());

        assertNotNull(beerDT_O1);

    }

    @Test
    void testCreateBeer() {

        var beerDTO = BeerDTO.builder()
                .beerName("Test Beer")
                .beerStyle(BeerStyle.IPA)
                .upc("123456789012")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();

        var savedBeerDTO = beerClient.createBeer(beerDTO);
        assertNotNull(savedBeerDTO);

    }

    @Test
    void testUpdateBeer() {

        var beerDTO = BeerDTO.builder()
                .beerName("Test Beer 65")
                .beerStyle(BeerStyle.GOSE)
                .upc("123545646456789012")
                .price(new BigDecimal("187.99"))
                .quantityOnHand(500)
                .build();

        var savedBeerDTO = beerClient.createBeer(beerDTO);
        final String newName = "Updated Test Beer 987";

        savedBeerDTO.setPrice(new BigDecimal("15.99"));
        savedBeerDTO.setBeerName(newName);

        var updatedBeerDTO = beerClient.updateBeer(savedBeerDTO);

        assertEquals(newName, updatedBeerDTO.getBeerName());
        assertEquals(savedBeerDTO.getPrice(), updatedBeerDTO.getPrice());

    }

    @Test
    void testDeleteBeer() {

        var beerDTO = BeerDTO.builder()
                .beerName("Test Beer 65")
                .beerStyle(BeerStyle.GOSE)
                .upc("123545646456789012")
                .price(new BigDecimal("187.99"))
                .quantityOnHand(500)
                .build();

        var savedBeerDTO = beerClient.createBeer(beerDTO);

        beerClient.deleteBeerById(savedBeerDTO.getId());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.getBeerById(savedBeerDTO.getId());
        });
    }
}