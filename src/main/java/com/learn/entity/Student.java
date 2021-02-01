package com.learn.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "student")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer value;

  @Version
  private Integer version;
}
