package org.ormi.priv.tfa.orderflow.productregistry.infra.api;

import java.net.URI;
import java.util.UUID;

import org.jboss.resteasy.reactive.RestResponse;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.RegisterProductCommandDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductDescriptionParamsDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductNameParamsDto;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.RetireProductCommand;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.UpdateProductDescriptionCommand;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.UpdateProductNameCommand;
import org.ormi.priv.tfa.orderflow.productregistry.application.RegisterProductService;
import org.ormi.priv.tfa.orderflow.productregistry.application.RetireProductService;
import org.ormi.priv.tfa.orderflow.productregistry.application.UpdateProductService;
import org.ormi.priv.tfa.orderflow.productregistry.infra.web.dto.CommandDtoMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

/**
 * Ressource REST des commandes du registre de produits.
 *
 * Traduit les DTO entrants en commandes applicatives et delegue aux services
 * de creation, retrait et mise a jour.
 */

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductRegistryCommandResource {

    /** Mapper DTO vers commandes. */
    private final CommandDtoMapper mapper;
    /** Service de creation de produits. */
    private final RegisterProductService registerProductService;
    /** Service de retrait de produits. */
    private final RetireProductService retireProductService;
    /** Service de mise a jour de produits. */
    private final UpdateProductService updateProductService;

    /**
     * Construit la ressource avec ses dependances.
     *
     * @param commandDtoMapper mapper DTO vers commandes
     * @param registerService service d'enregistrement
     * @param retireService service de retrait
     * @param updateService service de mise a jour
     */
    @Inject
    public ProductRegistryCommandResource(
        final CommandDtoMapper commandDtoMapper,
        final RegisterProductService registerService,
        final RetireProductService retireService,
        final UpdateProductService updateService
    ) {
        this.mapper = commandDtoMapper;
        this.registerProductService = registerService;
        this.retireProductService = retireService;
        this.updateProductService = updateService;
    }

    /**
     * Enregistre un nouveau produit.
     *
     * @param cmd commande DTO de creation
     * @param uriInfo informations d'URI pour la creation
     * @return reponse 201 avec l'emplacement
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<Void> registerProduct(
        final RegisterProductCommandDto cmd,
        @Context final UriInfo uriInfo
    ) {
        if (isBlank(cmd == null ? null : cmd.name())
            || isBlank(cmd.description())
            || isBlank(cmd.skuId())) {
            return RestResponse.status(RestResponse.Status.BAD_REQUEST);
        }
        final ProductId productId = registerProductService
            .handle(mapper.toCommand(cmd));
        final URI location = uriInfo.getAbsolutePathBuilder()
            .path("/products/" + productId.value())
            .build();
        return RestResponse.created(location);
    }

    /**
     * Retire un produit.
     *
     * @param productId identifiant du produit
     * @return reponse 204
     */
    @DELETE
    @Path("/{id}")
    public RestResponse<Void> retireProduct(
        @PathParam("id") final String productId
    ) {
        ProductId parsedId = new ProductId(UUID.fromString(productId));
        retireProductService.retire(new RetireProductCommand(parsedId));
        return RestResponse.noContent();
    }

    /**
     * Met a jour le nom d'un produit.
     *
     * @param productId identifiant du produit
     * @param params donnees de mise a jour du nom
     * @return reponse 204
     */
    @PATCH
    @Path("/{id}/name")
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<Void> updateProductName(
        @PathParam("id") final String productId,
        final UpdateProductNameParamsDto params
    ) {
        if (isBlank(params == null ? null : params.name())) {
            return RestResponse.status(RestResponse.Status.BAD_REQUEST);
        }
        ProductId parsedId = new ProductId(UUID.fromString(productId));
        updateProductService.handle(
            new UpdateProductNameCommand(parsedId, params.name())
        );
        return RestResponse.noContent();
    }

    /**
     * Met a jour la description d'un produit.
     *
     * @param productId identifiant du produit
     * @param params donnees de mise a jour de description
     * @return reponse 204
     */
    @PATCH
    @Path("/{id}/description")
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<Void> updateProductDescription(
        @PathParam("id") final String productId,
        final UpdateProductDescriptionParamsDto params
    ) {
        if (isBlank(params == null ? null : params.description())) {
            return RestResponse.status(RestResponse.Status.BAD_REQUEST);
        }
        ProductId parsedId = new ProductId(UUID.fromString(productId));
        updateProductService.handle(
            new UpdateProductDescriptionCommand(
                parsedId,
                params.description()
            )
        );
        return RestResponse.noContent();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
