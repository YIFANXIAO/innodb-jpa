package com.learn.api;

import com.learn.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PatchMapping
    void updateTeacherWithPessimisticLock() {
        teacherService.updateTeacherWithPessimisticLock();
    }

    @PatchMapping(value = "/em/find")
    void updateTeacherWithPessimisticLockByFind() {
        teacherService.updateTeacherWithPessimisticLockByFind(1);
    }

    @PatchMapping(value = "/em/query")
    void updateTeacherWithPessimisticLockByQuery() {
        teacherService.updateTeacherWithPessimisticLockByQuery(1);
    }

    @PatchMapping(value = "/em/explicit")
    void updateTeacherWithPessimisticLockByExplicitLocking() {
        teacherService.updateTeacherWithPessimisticLockByExplicitLocking(1);
    }

}
