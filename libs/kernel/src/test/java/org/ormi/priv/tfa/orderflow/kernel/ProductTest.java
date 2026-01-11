package org.ormi.priv.tfa.orderflow.kernel;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductLifecycle;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private static SkuId sku(String value) {
        return new SkuId(value);
    }

    @Test
    void create_validProduct_returnsProduct_andDoesNotThrow_andIsActive() {
        Product p = assertDoesNotThrow(() ->
                Product.create("Produit A", "Description A", sku("SKU-00001"))
        );

        assertNotNull(p);
        assertNotNull(p.getId());
        assertEquals("Produit A", p.getName());
        assertEquals("Description A", p.getDescription());
        assertNotNull(p.getSkuId());
        assertEquals(ProductLifecycle.ACTIVE, p.getStatus());
        assertEquals(1L, p.getVersion());
    }

    @Test
    void create_invalidName_null_throwsConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () ->
                Product.create(null, "Description A", sku("SKU-00001"))
        );
    }

    @Test
    void create_invalidName_blank_throwsConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () ->
                Product.create("   ", "Description A", sku("SKU-00001"))
        );
    }

    @Test
    void create_invalidDescription_null_throwsConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () ->
                Product.create("Produit A", null, sku("SKU-00001"))
        );
    }

    @Test
    void create_invalidSkuId_null_throwsConstraintViolationException() {
        assertThrows(ConstraintViolationException.class, () ->
                Product.create("Produit A", "Description A", null)
        );
    }

    @Test
    void update_withInvalidInputs_throwsConstraintViolationException() {
        Product p = Product.create("Produit A", "Description A", sku("SKU-00001"));

        assertThrows(ConstraintViolationException.class, () -> p.updateName(null));

        assertThrows(ConstraintViolationException.class, () -> p.updateName("   "));

        assertThrows(ConstraintViolationException.class, () -> p.updateDescription(null));
    }

    @Test
    void update_activeProduct_doesNotThrow_andUpdatesFields() {
        Product p = Product.create("Produit A", "Description A", sku("SKU-00001"));

        assertDoesNotThrow(() -> p.updateName("Produit B"));
        assertEquals("Produit B", p.getName());
        assertEquals(ProductLifecycle.ACTIVE, p.getStatus());

        assertDoesNotThrow(() -> p.updateDescription("Description B"));
        assertEquals("Description B", p.getDescription());
        assertEquals(ProductLifecycle.ACTIVE, p.getStatus());

        assertEquals(3L, p.getVersion());
    }

    @Test
    void update_retiredProduct_throwsIllegalStateException() {
        Product p = Product.create("Produit A", "Description A", sku("SKU-00001"));
        assertDoesNotThrow(p::retire);
        assertEquals(ProductLifecycle.RETIRED, p.getStatus());

        assertThrows(IllegalStateException.class, () -> p.updateName("X"));

        assertThrows(IllegalStateException.class, () -> p.updateDescription("Y"));
    }

    @Test
    void retire_activeProduct_doesNotThrow_andSetsRetired() {
        Product p = Product.create("Produit A", "Description A", sku("SKU-00001"));

        assertDoesNotThrow(p::retire);
        assertEquals(ProductLifecycle.RETIRED, p.getStatus());
        assertEquals(2L, p.getVersion());
    }

    @Test
    void retire_retiredProduct_throwsIllegalStateException() {
        Product p = Product.create("Produit A", "Description A", sku("SKU-00001"));
        assertDoesNotThrow(p::retire);
        assertEquals(ProductLifecycle.RETIRED, p.getStatus());

        assertThrows(IllegalStateException.class, p::retire);
    }

    
}
