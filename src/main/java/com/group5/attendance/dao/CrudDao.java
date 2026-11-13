package com.group5.attendance.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic DAO contract (Generics - syllabus).
 * T = the entity type, ID = the type of its primary key.
 */
public interface CrudDao<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();
}