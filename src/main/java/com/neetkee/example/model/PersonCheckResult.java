package com.neetkee.example.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class PersonCheckResult {
    @Id
    private Integer personId;
    private Boolean checkResult;
}
