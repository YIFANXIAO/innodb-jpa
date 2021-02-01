package com.learn.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

@Entity
@Table(name = "teacher")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedQuery(name="updateTeacherByIdWithNameQuery",
    query="SELECT t FROM Teacher t WHERE t.id = :id",
    lockMode = PESSIMISTIC_WRITE)
public class Teacher {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer teacherNumber;

  private Integer value;

}
