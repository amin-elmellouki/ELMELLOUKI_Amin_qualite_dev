package org.ormi.priv.tfa.orderflow.productregistry.application;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.EventLogEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.OutboxEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.EventLogRepository;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.OutboxRepository;
import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductDescriptionUpdated;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductNameUpdated;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductRepository;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.UpdateProductDescriptionCommand;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.UpdateProductNameCommand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service applicatif de mise a jour de produit.
 *
 * Charge l'agregat, applique la modification (nom ou description),
 * persiste l'etat et publie l'evenement via le journal et l'outbox.
 */

@ApplicationScoped
public class UpdateProductService {

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
    public UpdateProductService(
        final ProductRepository productRepository,
        final EventLogRepository eventLogRepository,
        final OutboxRepository outboxRepository
    ) {
        this.repository = productRepository;
        this.eventLog = eventLogRepository;
        this.outbox = outboxRepository;
    }

    /**
     * Met a jour le nom d'un produit.
     *
     * @param cmd commande de mise a jour du nom
     * @throws IllegalArgumentException si le produit n'existe pas
     */
    @Transactional
    public void handle(final UpdateProductNameCommand cmd)
        throws IllegalArgumentException {
        Product product = repository.findById(cmd.productId())
            .orElseThrow(
                () -> new IllegalArgumentException("Product not found")
            );
        EventEnvelope<ProductNameUpdated> event = product.updateName(
            cmd.newName()
        );
        // Save domain object
        repository.save(product);
        // Append event to event log
        final EventLogEntity persistedEvent = eventLog.append(event);
        // Publish event to outbox
        outbox.publish(
            OutboxEntity.Builder()
                .sourceEvent(persistedEvent)
                .build()
        );
    }

    /**
     * Met a jour la description d'un produit.
     *
     * @param cmd commande de mise a jour de description
     * @throws IllegalArgumentException si le produit n'existe pas
     */
    @Transactional
    public void handle(final UpdateProductDescriptionCommand cmd)
        throws IllegalArgumentException {
        Product product = repository.findById(cmd.productId())
            .orElseThrow(
                () -> new IllegalArgumentException("Product not found")
            );
        EventEnvelope<ProductDescriptionUpdated> event =
            product.updateDescription(cmd.newDescription());
        // Save domain object
        repository.save(product);
        // Append event to event log
        final EventLogEntity persistedEvent = eventLog.append(event);
        // Publish event to outbox
        outbox.publish(
            OutboxEntity.Builder()
                .sourceEvent(persistedEvent)
                .build()
        );
    }
}
