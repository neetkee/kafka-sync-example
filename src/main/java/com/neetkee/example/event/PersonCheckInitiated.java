package com.neetkee.example.event;

import com.neetkee.example.model.Person;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersonCheckInitiated {
    private Person person;
}
