package org.ormi.priv.tfa.orderflow.productregistry.application;

import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;

/**
 * Commandes applicatives du registre de produits.
 *
 * Interface scellee pour regrouper les types de commandes executes par
 * les services d'application.
 */

public sealed interface ProductCommand {
    record RegisterProductCommand(
        String name,
        String description,
        SkuId skuId
    ) implements ProductCommand {
    }

    record RetireProductCommand(
        ProductId productId
    ) implements ProductCommand {
    }

    record UpdateProductNameCommand(
        ProductId productId,
        String newName
    ) implements ProductCommand {
    }

    record UpdateProductDescriptionCommand(
        ProductId productId,
        String newDescription
    ) implements ProductCommand {
    }
}
