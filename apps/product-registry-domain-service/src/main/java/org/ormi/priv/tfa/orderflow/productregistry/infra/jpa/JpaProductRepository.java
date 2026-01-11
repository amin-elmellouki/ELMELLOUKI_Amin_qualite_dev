package org.ormi.priv.tfa.orderflow.productregistry.infra.jpa;

import java.util.Optional;
import java.util.UUID;

import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductRepository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Repository JPA pour la persistance des produits.
 *
 * Mappe l'agregat de domaine vers l'entite JPA et inversement.
 */

@ApplicationScoped
public final class JpaProductRepository
    implements PanacheRepositoryBase<ProductEntity, UUID>,
        ProductRepository {

    /** Mapper entre agregat de domaine et entite JPA. */
    private final ProductJpaMapper mapper;
    /** Mapper d'identifiant produit. */
    private final ProductIdMapper productIdMapper;
    /** Mapper d'identifiant SKU. */
    private final SkuIdMapper skuIdMapper;

    /**
     * Construit le repository JPA.
     *
     * @param productJpaMapper mapper produit <-> entite
     * @param productIdMapperInstance mapper d'identifiant produit
     * @param skuIdMapperInstance mapper de SKU
     */
    @Inject
    public JpaProductRepository(
        final ProductJpaMapper productJpaMapper,
        final ProductIdMapper productIdMapperInstance,
        final SkuIdMapper skuIdMapperInstance
    ) {
        this.mapper = productJpaMapper;
        this.productIdMapper = productIdMapperInstance;
        this.skuIdMapper = skuIdMapperInstance;
    }

    @Override
    @Transactional
    public void save(final Product product) {
        findByIdOptional(productIdMapper.map(product.getId()))
            .ifPresentOrElse(
                entity -> mapper.updateEntity(product, entity),
                () -> {
                    ProductEntity newEntity = mapper.toEntity(product);
                    getEntityManager().merge(newEntity);
                }
            );
    }

    @Override
    public Optional<Product> findById(final ProductId id) {
        return findByIdOptional(productIdMapper.map(id))
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsBySkuId(final SkuId skuId) {
        return count("skuId", skuIdMapper.map(skuId)) > 0;
    }
}
