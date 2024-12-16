package com.minierpapp.model.common.mapper;

import org.mapstruct.MappingTarget;

public interface BaseMapper<E, D, Q, S> {
    E toEntity(Q request);
    D toDto(E entity);
    S toResponse(E entity);
    Q toRequest(D dto);
    void updateEntity(Q request, @MappingTarget E entity);
}