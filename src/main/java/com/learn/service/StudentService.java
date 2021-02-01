package com.learn.service;

import com.learn.entity.Student;
import com.learn.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public void updateStudentWithOptimisticLock() {
        Student student = studentRepository.findById(1).get();
        student.setValue(2);
        studentRepository.save(student);
    }
}
