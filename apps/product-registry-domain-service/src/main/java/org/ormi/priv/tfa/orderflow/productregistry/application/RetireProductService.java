package org.ormi.priv.tfa.orderflow.productregistry.application;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.EventLogEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.OutboxEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.EventLogRepository;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.OutboxRepository;
import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductRetired;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductRepository;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.RetireProductCommand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service applicatif pour retirer un produit du catalogue.
 *
 * Charge le produit par son identifiant, exécute la transition métier
 * de retrait, persiste l'agrégat, puis journalise et publie l'événement
 * de domaine via le journal des événements et l'outbox.
 */
@ApplicationScoped
public class RetireProductService {

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
    public RetireProductService(
        final ProductRepository productRepository,
        final EventLogRepository eventLogRepository,
        final OutboxRepository outboxRepository
    ) {
        this.repository = productRepository;
        this.eventLog = eventLogRepository;
        this.outbox = outboxRepository;
    }

    /**
     * Retire un produit en appliquant la transition metier.
     *
     * @param cmd commande de retrait
     * @throws IllegalArgumentException si le produit n'existe pas
     */
    @Transactional
    public void retire(final RetireProductCommand cmd)
        throws IllegalArgumentException {
        Product product = repository.findById(cmd.productId())
            .orElseThrow(
                () -> new IllegalArgumentException("Product not found")
            );
        EventEnvelope<ProductRetired> event = product.retire();
        repository.save(product);
        // Append event to the log
        final EventLogEntity persistedEvent = eventLog.append(event);
        // Publish outbox
        outbox.publish(
            OutboxEntity.Builder()
                .sourceEvent(persistedEvent)
                .build()
        );
    }
}
