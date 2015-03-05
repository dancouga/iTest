package com.cht.iTest.util;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

@Repository
public class JPAHelper {

	private static EntityManager entityManager;

	private JPAHelper() {

	}

	@PersistenceContext
	private void setEntityManager(EntityManager entityManager) {
		JPAHelper.entityManager = entityManager;
	}

	public static <T> List<T> findAllEntities(Class<T> clazz) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> root = cq.from(clazz);
		return entityManager.createQuery(cq.select(root)).getResultList();
	}

	public static <T> T findEntity(Class<T> clazz, Object id) {
		return entityManager.find(clazz, id);
	}

	public static <T> T save(T entiry) {
		return entityManager.merge(entiry);
	}

	public static <T> void delete(Class<T> clazz, Object id) {
		T t = findEntity(clazz, id);

		if (t != null) {
			entityManager.remove(t);
		}
	}

	public static <T> void delete(T entiry) {
		entiry = entityManager.merge(entiry);
		entityManager.remove(entiry);
	}

}
