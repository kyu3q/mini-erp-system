package com.minierpapp.model.item.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemDto;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.Collections;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {Collections.class}
)
public interface ItemMapper extends BaseMapper<Item, ItemDto, ItemRequest, ItemResponse> {
    @Override
    Item toEntity(ItemDto dto);

    @Override
    ItemDto toDto(Item entity);

    @Override
    Item requestToEntity(ItemRequest request);

    @Override
    ItemResponse entityToResponse(Item entity);

    @Override
    void updateEntityFromRequest(ItemRequest request, @MappingTarget Item entity);

    @Override
    void updateEntity(ItemDto dto, @MappingTarget Item entity);

    @Override
    ItemRequest responseToRequest(ItemResponse response);
}