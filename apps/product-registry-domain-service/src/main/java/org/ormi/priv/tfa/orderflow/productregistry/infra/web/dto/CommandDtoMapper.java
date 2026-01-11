package org.ormi.priv.tfa.orderflow.productregistry.infra.web.dto;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.RegisterProductCommand;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.RegisterProductCommandDto;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuIdMapper;

/**
 * Mapper MapStruct entre DTO de commande et commande applicative.
 */

@Mapper(
    componentModel = "cdi",
    builder = @Builder(disableBuilder = true),
    uses = { SkuIdMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CommandDtoMapper {
    /**
     * Convertit un DTO de commande en commande applicative.
     *
     * @param dto DTO entrant
     * @return commande applicative
     */
    RegisterProductCommand toCommand(RegisterProductCommandDto dto);

    /**
     * Convertit une commande applicative en DTO.
     *
     * @param command commande applicative
     * @return DTO de commande
     */
    RegisterProductCommandDto toDto(RegisterProductCommand command);
}
