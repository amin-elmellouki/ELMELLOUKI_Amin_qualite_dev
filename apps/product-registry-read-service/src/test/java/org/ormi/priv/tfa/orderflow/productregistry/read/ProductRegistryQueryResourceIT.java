package org.ormi.priv.tfa.orderflow.productregistry.read.infra.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView;
import org.ormi.priv.tfa.orderflow.productregistry.read.application.ReadProductService;
import org.ormi.priv.tfa.orderflow.productregistry.read.application.ReadProductService.SearchPaginatedResult;
import org.ormi.priv.tfa.orderflow.productregistry.read.infra.web.dto.ProductSummaryDtoMapper;
import org.ormi.priv.tfa.orderflow.productregistry.read.infra.web.dto.ProductViewDtoMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;

@QuarkusTest
class ProductRegistryQueryResourceIT {

    @InjectMock ReadProductService readProductService;
    @InjectMock ProductViewDtoMapper productViewDtoMapper;
    @InjectMock ProductSummaryDtoMapper productSummaryDtoMapper;
    @InjectMock ProductIdMapper productIdMapper;

    @Test
    void get_products_filterWithMatch_returns200_andList() {
        Mockito.when(readProductService.searchProducts(Mockito.eq("SKU"), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(new SearchPaginatedResult(List.of(), 0L));

        given()
            .queryParam("sku", "SKU")
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("products", notNullValue());
    }

    @Test
    void get_products_filterNoMatch_returns200_andEmptyList() {
        Mockito.when(readProductService.searchProducts(Mockito.eq("NOPE"), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(new SearchPaginatedResult(List.of(), 0L));

        given()
            .queryParam("sku", "NOPE")
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("products.size()", is(0));
    }

    @Test
    void get_products_noFilter_returns200() {
        Mockito.when(readProductService.searchProducts(Mockito.eq(""), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(new SearchPaginatedResult(List.of(), 0L));

        given()
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200);
    }

    @Test
    void get_products_byId_existing_returns200_andProduct() {
        UUID id = UUID.randomUUID();
        Mockito.when(productIdMapper.map(Mockito.eq(id))).thenReturn(null); // on ne teste pas le mapper ici

        ProductView view = Mockito.mock(ProductView.class);
        Mockito.when(readProductService.findById(Mockito.any()))
            .thenReturn(Optional.of(view));

        ProductViewDto dto = Mockito.mock(ProductViewDto.class);
        Mockito.when(productViewDtoMapper.toDto(view)).thenReturn(dto);

        given()
        .when()
            .get("/api/products/{id}", id.toString())
        .then()
            .statusCode(200);
    }

    @Test
    void get_products_byId_nonExisting_returns404() {
        UUID id = UUID.randomUUID();
        Mockito.when(productIdMapper.map(Mockito.eq(id))).thenReturn(null);
        Mockito.when(readProductService.findById(Mockito.any()))
            .thenReturn(Optional.empty());

        given()
        .when()
            .get("/api/products/{id}", id.toString())
        .then()
            .statusCode(404);
    }
}
