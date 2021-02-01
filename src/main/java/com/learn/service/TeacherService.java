package com.learn.service;

import com.learn.entity.Teacher;
import com.learn.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherService {

    @PersistenceContext
    EntityManager entityManager;

    private final TeacherRepository teacherRepository;

    public void updateTeacherWithPessimisticLock() {
        Teacher teacher = teacherRepository.findAll().stream().findFirst().get();
        teacher.setValue(teacher.getValue() + 1);
        teacherRepository.save(teacher);
    }

    public void updateTeacherWithPessimisticLockByFind(Integer id) {
        log.info("====================em find start======================");
        Teacher teacher = entityManager.find(Teacher.class, id, LockModeType.PESSIMISTIC_WRITE);
        teacher.setValue(teacher.getValue() + 1);
        teacherRepository.save(teacher);
    }

    public void updateTeacherWithPessimisticLockByQuery(Integer id) {
        log.info("====================em query start======================");

        Query query = entityManager.createQuery("from Teacher where id = :id");
        query.setParameter("id", id);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        Teacher resultList = (Teacher) query.getResultList().get(0);
        log.info("query result : {}", resultList.getValue());
    }

    public void updateTeacherWithPessimisticLockByExplicitLocking(Integer id) {
        log.info("====================em Explicit Locking start======================");
        Teacher teacher = entityManager.find(Teacher.class, id);
        entityManager.lock(teacher, LockModeType.PESSIMISTIC_WRITE);
    }

    public void updateTeacherWithPessimisticLockByRefresh(Integer id) {
        log.info("====================em refresh start======================");
        Teacher teacher = entityManager.find(Teacher.class, id);
        entityManager.refresh(teacher, LockModeType.PESSIMISTIC_WRITE);
    }

    public void updateTeacherWithPessimisticLockByNameQuery(Integer id) {
        log.info("====================em name query start======================");
        Query updateTeacherByIdWithNameQuery = entityManager.createNamedQuery("updateTeacherByIdWithNameQuery");
        updateTeacherByIdWithNameQuery.setParameter("id", 1);
        updateTeacherByIdWithNameQuery.getResultList();
    }

}
