package com.spring.batch.config;

import com.spring.batch.entity.Person;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class PersonProcessor implements ItemProcessor<Person,Person> {

    @Override
    public @Nullable Person process(Person item) throws Exception {

        item.setJobTitle(item.getJobTitle().toUpperCase());
        item.setFirstName(item.getFirstName().toUpperCase());
        return item;
    }
}
