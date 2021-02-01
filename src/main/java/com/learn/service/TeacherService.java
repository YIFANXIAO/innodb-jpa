package com.learn.service;

import com.learn.entity.Teacher;
import com.learn.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public void updateTeacherWithPessimisticLock() {
        Teacher teacher = teacherRepository.findById(1).get();
        teacher.setValue(2);
        teacherRepository.save(teacher);
    }
}
