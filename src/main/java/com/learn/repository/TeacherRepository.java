package com.learn.repository;

import com.learn.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Override
    Optional<Teacher> findById(Integer integer);
}
