package com.minierpapp.model.base;

import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

/**
 * 基本的なマッピング操作を定義するインターフェース
 *
 * @param <E> エンティティの型
 * @param <D> DTOの型
 * @param <Q> リクエストの型
 * @param <R> レスポンスの型
 */
public interface BaseMapper<E, D, Q, R> {
    /**
     * DTOからエンティティへの変換
     */
    @Mappings({
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    E toEntity(D dto);

    /**
     * エンティティからDTOへの変換
     */
    D toDto(E entity);

    /**
     * リクエストからエンティティへの変換
     */
    @Mappings({
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    E requestToEntity(Q request);

    /**
     * エンティティからレスポンスへの変換
     */
    R entityToResponse(E entity);

    /**
     * レスポンスからリクエストへの変換
     */
    Q responseToRequest(R response);
    /**
     * リクエストからエンティティの更新
     */
    void updateEntityFromRequest(Q request, @MappingTarget E entity);

    /**
     * DTOからエンティティの更新
     */
    void updateEntity(D dto, @MappingTarget E entity);
} 