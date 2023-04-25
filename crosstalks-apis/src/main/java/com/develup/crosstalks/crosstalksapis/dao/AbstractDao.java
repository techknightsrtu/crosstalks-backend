package com.develup.crosstalks.crosstalksapis.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;


public abstract class AbstractDao<T, ID extends Serializable> {

  private static final int BATCH_SIZE = 1000;
  @Autowired
  private SessionFactory sessionFactory;
  private Class<T> persistentClass;

  public AbstractDao() {
    this.persistentClass = (Class<T>) (((ParameterizedType) getClass()
        .getGenericSuperclass()).getActualTypeArguments()[0]);
  }

  public Session getCurrentSession() {
    return sessionFactory.getCurrentSession();
  }

  public ID save(T element) {
    Session session = getCurrentSession();
    return (ID) session.save(element);
  }

  public void persist(T element) {
    Session session = getCurrentSession();
    session.persist(element);
  }

  public void update(T updateElement) {
    Session session = getCurrentSession();
    session.update(updateElement);
  }

  public void delete(T t) {
    Session session = getCurrentSession();
    session.delete(t);
  }

  public void saveOrUpdate(T insertElement) {
    Session session = getCurrentSession();
    session.saveOrUpdate(insertElement);
  }

  public void merge(T insertElement) {
    Session session = getCurrentSession();
    session.merge(insertElement);
  }

  public void deleteById(ID id) {
    T deleteElement = getById(id);
    if (deleteElement != null) {
      delete(deleteElement);
    }
  }

  public void bulkInsert(List<T> collection) {
    Session session = getCurrentSession();
    int counter = 0;
    for (T t : collection) {
      session.persist(t);
      counter++;
      flushAndClear(collection, session, counter);
    }
  }

  private void flushAndClear(List<T> collection, Session session, int counter) {
    if (counter % BATCH_SIZE == 0 || counter == collection.size()) {
      session.flush();
      session.clear();
    }
  }

  public void bulkUpdate(List<T> collection) {
    Session session = getCurrentSession();
    int counter = 0;
    for (T t : collection) {
      session.update(t);
      counter++;
      flushAndClear(collection, session, counter);
    }
  }

  public void bulkMerge(List<T> collection) {
    Session session = getCurrentSession();
    int counter = 0;
    for (T t : collection) {
      session.merge(t);
      counter++;
      flushAndClear(collection, session, counter);
    }
  }

  public void bulkDelete(List<T> collection) {
    Session session = getCurrentSession();
    int counter = 0;
    for (T t : collection) {
      session.delete(t);
      counter++;
      flushAndClear(collection, session, counter);
    }
  }

  public void executeSQLUpdate(String update) {
    Session session = getCurrentSession();
    session.createSQLQuery(update).executeUpdate();
  }

  public void executeHQLUpdate(String update) {
    Session session = getCurrentSession();
    session.createQuery(update).executeUpdate();
  }

  public T getById(ID id) {
    Session session = getCurrentSession();
    return session.get(this.persistentClass, id);
  }

  public T getByExternalId(String externalId) {
    return getUniqueEntityByColumn("externalId", externalId);
  }

  public Optional<T> getByExternalIdOptional(String externalId) {
    return getUniqueEntityByColumnOptional("externalId", externalId);
  }

  public List getBySqlQuery(String sqlQuery) {
    return getCurrentSession().createSQLQuery(sqlQuery).list();
  }

  public List<T> getEntitiesByColumn(String columnName, Object columnValue) {
    Query<T> query = getQuery(columnName, columnValue);
    return query.getResultList();
  }

  public T getUniqueEntityByColumn(String columnName, Object columnValue) {
    Query<T> query = getQuery(columnName, columnValue);
    return query.getSingleResult();
  }

  public Optional<T> getUniqueEntityByColumnOptional(String columnName, Object columnValue) {
    Query<T> query = getQuery(columnName, columnValue);
    return getSingleResult(query);
  }

  private Query<T> getQuery(String columnName, Object columnValue) {
    CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(this.persistentClass);
    Root<T> root = criteriaQuery.from(this.persistentClass);
    criteriaQuery.select(root).where(criteriaBuilder.equal(root.get(columnName), columnValue));
    return getCurrentSession().createQuery(criteriaQuery);
  }

  protected Optional<T> getSingleResult(Query<T> query) {
    try {
      return Optional.of(query.getSingleResult());
    } catch (EmptyResultDataAccessException | NoResultException e) {
      return Optional.empty();
    }
  }

  protected Optional<T> getSingleResult(CriteriaQuery<T> criteriaQuery, Root<T> root, List<Predicate> predicateList) {
    criteriaQuery.select(root).where(predicateList.toArray(new Predicate[]{}));
    return getSingleResult(getCurrentSession().createQuery(criteriaQuery));
  }

  public Optional<T> getEntityByParameter(Map<String, Object> entityParams) {

    CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(this.persistentClass);
    Root<T> root = criteriaQuery.from(this.persistentClass);

    List<Predicate> predicateList = new ArrayList<>();
    entityParams.forEach((attributeName, attributeValue) -> predicateList
        .add(criteriaBuilder.equal(root.get(attributeName), attributeValue)));

    return getSingleResult(criteriaQuery, root, predicateList);
  }

  public List<T> getEntitiesByParameter(Map<String, Object> entityParams) {
    Session session = getCurrentSession();
    CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
    CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(this.persistentClass);
    Root<T> root = criteriaQuery.from(this.persistentClass);

    List<Predicate> predicateList = new ArrayList<>();
    entityParams.forEach((attributeName, attributeValue) -> predicateList
        .add(criteriaBuilder.equal(root.get(attributeName), attributeValue)));

    criteriaQuery.select(root).where(predicateList.toArray(new Predicate[]{}));
    Query<T> query = session.createQuery(criteriaQuery);
    return query.getResultList();
  }

  public List<T> getEntitiesByInColumn(String columnName, Collection columnValues) {
    if (CollectionUtils.isEmpty(columnValues)) {
      return new ArrayList<>();
    }
    Session session = getCurrentSession();
    CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
    CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(this.persistentClass);
    Root<T> root = criteriaQuery.from(this.persistentClass);
    criteriaQuery.select(root).where(root.get(columnName).in(columnValues));
    Query<T> query = session.createQuery(criteriaQuery);
    return query.getResultList();
  }
}
