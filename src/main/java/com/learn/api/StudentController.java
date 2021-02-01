package com.learn.api;

import com.learn.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService optimisticLockTestService;

    @PatchMapping
    public void updateStudentWithOptimisticLock() {
        optimisticLockTestService.updateStudentWithOptimisticLock();
    }

}
