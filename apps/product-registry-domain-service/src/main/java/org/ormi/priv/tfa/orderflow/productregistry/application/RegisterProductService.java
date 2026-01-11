package org.ormi.priv.tfa.orderflow.productregistry.application;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.EventLogEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.OutboxEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.EventLogRepository;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.OutboxRepository;
import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductRegistered;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductRepository;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.RegisterProductCommand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service applicatif de creation de produit.
 *
 * Verifie l'unicite du SKU, cree l'agregat, persiste l'etat et publie
 * l'evenement via le journal et l'outbox.
 */

@ApplicationScoped
public class RegisterProductService {

    /** Repository de persistance des produits. */
    private final ProductRepository repository;
    /** Journal des evenements pour la publication. */
    private final EventLogRepository eventLog;
    /** Outbox de publication des evenements. */
    private final OutboxRepository outbox;

    /**
     * Construit le service avec ses dependances.
     *
     * @param productRepository depot des produits
     * @param eventLogRepository journal des evenements
     * @param outboxRepository outbox de publication
     */
    @Inject
    public RegisterProductService(
        final ProductRepository productRepository,
        final EventLogRepository eventLogRepository,
        final OutboxRepository outboxRepository
    ) {
        this.repository = productRepository;
        this.eventLog = eventLogRepository;
        this.outbox = outboxRepository;
    }

    /**
     * Enregistre un nouveau produit.
     *
     * @param cmd commande de creation
     * @return identifiant du produit cree
     * @throws IllegalArgumentException si le SKU existe deja
     */
    @Transactional
    public ProductId handle(final RegisterProductCommand cmd)
        throws IllegalArgumentException {
        if (repository.existsBySkuId(cmd.skuId())) {
            throw new IllegalArgumentException(
                String.format("SKU already exists: %s", cmd.skuId())
            );
        }
        Product product = Product.create(
            cmd.name(),
            cmd.description(),
            cmd.skuId()
        );
        // Save domain object
        repository.save(product);
        EventEnvelope<ProductRegistered> event = EventEnvelope.with(
            new ProductRegistered(
                product.getId(),
                product.getSkuId(),
                cmd.name(),
                cmd.description()
            ),
            product.getVersion()
        );
        // Appends event to the log
        final EventLogEntity persistedEvent = eventLog.append(event);
        // Publish outbox
        outbox.publish(
            OutboxEntity.Builder()
                .sourceEvent(persistedEvent)
                .build()
        );
        return product.getId();
    }
}
