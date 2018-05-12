package com.neetkee.example.event;

import lombok.Data;

@Data
public class PersonChecked {
    private Integer personId;
    private Boolean checkResult;
}
