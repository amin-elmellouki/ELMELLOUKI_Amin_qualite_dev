package org.ormi.priv.tfa.orderflow.productregistry.infra.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.RegisterProductCommandDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductNameParamsDto;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.productregistry.application.RegisterProductService;
import org.ormi.priv.tfa.orderflow.productregistry.application.RetireProductService;
import org.ormi.priv.tfa.orderflow.productregistry.application.UpdateProductService;
import org.ormi.priv.tfa.orderflow.productregistry.infra.web.dto.CommandDtoMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
class ProductRegistryCommandResourceIT {

    @InjectMock CommandDtoMapper mapper;
    @InjectMock RegisterProductService registerProductService;
    @InjectMock RetireProductService retireProductService;
    @InjectMock UpdateProductService updateProductService;

    @Test
    void post_products_valid_returns201_andLocationHeader() {
        UUID id = UUID.randomUUID();
        Mockito.when(mapper.toCommand(Mockito.any())).thenReturn(null); // on ne teste pas le mapping ici
        Mockito.when(registerProductService.handle(Mockito.any())).thenReturn(new ProductId(id));

        // ⚠️ Adapte les champs si ton DTO n’a pas exactement (name, description, sku)
        RegisterProductCommandDto dto = new RegisterProductCommandDto("Produit", "Desc", "SKU-0001");

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(dto)
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .header("Location", containsString(id.toString()));
    }

    @Test
    void post_products_invalidDto_returns400() {
        // DTO invalide : ex nom vide (selon validation côté DTO)
        RegisterProductCommandDto dto = new RegisterProductCommandDto("", "Desc", "SKU-0001");

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(dto)
        .when()
            .post("/api/products")
        .then()
            .statusCode(400);
    }

    @Test
    void post_products_nullBody_returns400() {
        given()
            .contentType(MediaType.APPLICATION_JSON)
        .when()
            .post("/api/products")
        .then()
            .statusCode(400);
    }

    @Test
    void patch_products_id_name_valid_returns204() {
        UUID id = UUID.randomUUID();
        UpdateProductNameParamsDto dto = new UpdateProductNameParamsDto("Nouveau nom");

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(dto)
        .when()
            .patch("/api/products/{id}/name", id.toString())
        .then()
            .statusCode(204);
    }

    @Test
    void patch_products_id_name_invalidDto_returns400() {
        UUID id = UUID.randomUUID();
        UpdateProductNameParamsDto dto = new UpdateProductNameParamsDto(""); // invalide si NotBlank

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(dto)
        .when()
            .patch("/api/products/{id}/name", id.toString())
        .then()
            .statusCode(400);
    }

    @Test
    void patch_products_id_name_nullBody_returns400() {
        UUID id = UUID.randomUUID();

        given()
            .contentType(MediaType.APPLICATION_JSON)
        .when()
            .patch("/api/products/{id}/name", id.toString())
        .then()
            .statusCode(400);
    }

    @Test
    void delete_products_existing_returns204() {
        UUID id = UUID.randomUUID();

        given()
        .when()
            .delete("/api/products/{id}", id.toString())
        .then()
            .statusCode(204);
    }

    @Test
    void delete_products_nonExisting_returns400() {
        // Ici, pour obtenir 400, il faut que le service lève une IllegalArgumentException / erreur métier mappée en 400.
        UUID id = UUID.randomUUID();
        Mockito.doThrow(new IllegalArgumentException("Not found"))
            .when(retireProductService)
            .retire(Mockito.any());

        given()
        .when()
            .delete("/api/products/{id}", id.toString())
        .then()
            .statusCode(400);
    }
}
