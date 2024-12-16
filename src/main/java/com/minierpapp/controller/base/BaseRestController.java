package com.minierpapp.controller.base;

import com.minierpapp.model.common.mapper.BaseMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseRestController<E, D, Q, S> {
    protected final BaseMapper<E, D, Q, S> mapper;

    protected BaseRestController(BaseMapper<E, D, Q, S> mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<S>> findAll() {
        return ResponseEntity.ok(findAllEntities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<S> findById(@PathVariable Long id) {
        return ResponseEntity.ok(findEntityById(id));
    }

    @PostMapping
    public ResponseEntity<S> create(@Valid @RequestBody Q request) {
        return new ResponseEntity<>(createEntity(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<S> update(@PathVariable Long id, @Valid @RequestBody Q request) {
        return ResponseEntity.ok(updateEntity(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteEntity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<S>> search(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(searchEntities(keyword));
    }

    protected abstract List<S> findAllEntities();
    protected abstract S findEntityById(Long id);
    protected abstract S createEntity(Q request);
    protected abstract S updateEntity(Long id, Q request);
    protected abstract void deleteEntity(Long id);
    protected abstract List<S> searchEntities(String keyword);
}