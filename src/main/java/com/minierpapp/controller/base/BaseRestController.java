package com.minierpapp.controller.base;

import com.minierpapp.model.base.BaseEntity;
import com.minierpapp.model.base.BaseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST APIの基本的なCRUD操作を提供する基底コントローラー
 *
 * @param <E> エンティティの型
 * @param <D> DTOの型
 * @param <Q> リクエストの型
 * @param <R> レスポンスの型
 */
public abstract class BaseRestController<E extends BaseEntity, D, Q, R> {

    protected final BaseMapper<E, D, Q, R> mapper;

    protected BaseRestController(BaseMapper<E, D, Q, R> mapper) {
        this.mapper = mapper;
    }

    /**
     * エンティティの一覧を取得
     */
    @GetMapping
    public ResponseEntity<List<R>> findAll() {
        List<E> entities = findAllEntities();
        List<R> responses = entities.stream()
                .map(mapper::entityToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * ページング付きでエンティティの一覧を取得
     */
    @GetMapping("/page")
    public ResponseEntity<Page<R>> findAll(Pageable pageable) {
        Page<E> entityPage = findAllEntities(pageable);
        Page<R> responsePage = entityPage.map(mapper::entityToResponse);
        return ResponseEntity.ok(responsePage);
    }

    /**
     * IDによるエンティティの取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<R> findById(@PathVariable Long id) {
        E entity = findEntityById(id);
        return ResponseEntity.ok(mapper.entityToResponse(entity));
    }

    /**
     * 新規エンティティの作成
     */
    @PostMapping
    public ResponseEntity<R> create(@Valid @RequestBody Q request) {
        E entity = mapper.requestToEntity(request);
        entity = createEntity(entity);
        return ResponseEntity.ok(mapper.entityToResponse(entity));
    }

    /**
     * 既存エンティティの更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<R> update(@PathVariable Long id, @Valid @RequestBody Q request) {
        E entity = findEntityById(id);
        mapper.updateEntityFromRequest(request, entity);
        entity = updateEntity(entity);
        return ResponseEntity.ok(mapper.entityToResponse(entity));
    }

    /**
     * エンティティの削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteEntity(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<R>> search(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(searchEntities(keyword));
    }

    protected abstract List<E> findAllEntities();
    protected abstract Page<E> findAllEntities(Pageable pageable);
    protected abstract E findEntityById(Long id);
    protected abstract E createEntity(E entity);
    protected abstract E updateEntity(E entity);
    protected abstract void deleteEntity(Long id);
    protected abstract List<R> searchEntities(String keyword);
}