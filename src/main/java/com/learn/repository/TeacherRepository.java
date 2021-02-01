package com.learn.repository;

import com.learn.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {

    @Lock(value = PESSIMISTIC_WRITE)
    @Override
    Optional<Teacher> findById(Integer integer);
}
