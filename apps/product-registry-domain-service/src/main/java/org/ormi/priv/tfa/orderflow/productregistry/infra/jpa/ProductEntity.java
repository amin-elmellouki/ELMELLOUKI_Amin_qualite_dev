package org.ormi.priv.tfa.orderflow.productregistry.infra.jpa;

import java.util.UUID;

import org.ormi.priv.tfa.orderflow.kernel.product.ProductLifecycle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entite JPA representant un produit dans la base.
 */

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(
    schema = "domain",
    name = "products",
    indexes = {
        @Index(name = "ux_products_sku", columnList = "sku", unique = true)
    })
public class ProductEntity {

    /** Longueur maximale du SKU. */
    private static final int SKU_LENGTH = 9;

    /** Identifiant technique du produit. */
    @Id
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "uuid"
    )
    private UUID id;
    /** Nom du produit. */
    @Column(
        name = "name",
        nullable = false,
        columnDefinition = "text"
    )
    private String name;
    /** Description du produit. */
    @Column(
        name = "description",
        nullable = false,
        columnDefinition = "text"
    )
    private String description;
    /** SKU du produit. */
    @Column(
        name = "sku_id",
        nullable = false,
        updatable = false,
        length = SKU_LENGTH,
        unique = true,
        columnDefinition = "varchar(9)"
    )
    private String skuId;
    /** Statut du cycle de vie. */
    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        columnDefinition = "text"
    )
    private ProductLifecycle status;
    /** Version pour le controle de concurrence. */
    @Column(
        name = "version",
        nullable = false,
        columnDefinition = "bigint"
    )
    private Long version;
}
