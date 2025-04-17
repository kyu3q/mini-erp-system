package com.minierpapp.model.base;

import org.mapstruct.MappingTarget;
import java.util.List;

/**
 * 基本的なマッピング操作を定義するインターフェース
 *
 * @param <E> エンティティの型
 * @param <D> DTOの型
 * @param <Q> リクエストの型
 * @param <R> レスポンスの型
 */
public interface BaseMapper<E extends BaseEntity, D, Q, R> {
    
    /**
     * DTOからエンティティへの変換
     */
    E toEntity(D dto);

    /**
     * エンティティからDTOへの変換
     */
    D toDto(E entity);

    /**
     * リクエストからエンティティへの変換
     */
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

    default List<R> toResponseList(List<E> entities) {
        if (entities == null) return null;
        return entities.stream()
            .map(this::entityToResponse)
            .toList();
    }
    
    default List<D> toDtoList(List<E> entities) {
        if (entities == null) return null;
        return entities.stream()
            .map(this::toDto)
            .toList();
    }
} 