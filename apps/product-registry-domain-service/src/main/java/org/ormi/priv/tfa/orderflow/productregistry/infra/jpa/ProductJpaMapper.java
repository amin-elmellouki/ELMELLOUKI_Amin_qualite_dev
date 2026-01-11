package org.ormi.priv.tfa.orderflow.productregistry.infra.jpa;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuIdMapper;

/**
 * Mapper MapStruct entre l'agregat Product et l'entite JPA.
 */

@Mapper(
    componentModel = "cdi",
    builder = @Builder(disableBuilder = false),
    uses = { ProductIdMapper.class, SkuIdMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class ProductJpaMapper {

    /**
     * Convertit une entite JPA en agregat domaine.
     *
     * @param entity entite JPA
     * @return agregat domaine
     */
    public abstract Product toDomain(ProductEntity entity);

    /**
     * Met a jour une entite existante a partir de l'agregat.
     *
     * @param product agregat domaine
     * @param entity entite JPA cible
     */
    public abstract void updateEntity(
        Product product,
        @MappingTarget ProductEntity entity
    );

    /**
     * Convertit un agregat domaine en entite JPA.
     *
     * @param product agregat domaine
     * @return entite JPA
     */
    public abstract ProductEntity toEntity(Product product);
}
