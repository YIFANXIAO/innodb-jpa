package com.learn.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "teacher")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer teacherNumber;

  private Integer value;

}
