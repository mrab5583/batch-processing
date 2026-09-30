package com.spring.batch.config;

import com.spring.batch.entity.Person;
import com.spring.batch.repository.PersonRepository;
import com.sun.source.tree.LineMap;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@EnableBatchProcessing
@Configuration
public class JobConfig {

    @Autowired
    private PersonRepository pRepository;

    @Bean
    public Job createJob(JobRepository jobRepository, Step step){
        return new JobBuilder("insertPersons",jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step createStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("step-InsertPersons",jobRepository)
                .<Person, Person>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    PersonProcessor processor(){
        return new PersonProcessor();
    }

    @Bean
    public FlatFileItemReader<Person> reader(){
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("people-1000.csv"))
                .linesToSkip(1)
                .lineMapper(lineMapper())
                .targetType(Person.class)
                .build();
    }

    @Bean
    public RepositoryItemWriter<Person> writer(){
        return new RepositoryItemWriterBuilder<Person>()
                .repository(pRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public LineMapper<Person> lineMapper(){

        DefaultLineMapper<Person> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","userId","firstName","lastName","gender","email","phone","dateOfBirth","jobTitle");

        BeanWrapperFieldSetMapper<Person> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Person.class);
        readingEveryLine(lineTokenizer,fieldSetMapper);

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);
        return defaultLineMapper;
    }

    private void readingEveryLine(DelimitedLineTokenizer lineTokenizer,BeanWrapperFieldSetMapper<Person> fieldSetMapper){

        LineMapper<Person> lineMapper = (line,number) -> {

            System.out.println("==========CSV Line=====" + line);
            System.out.println("RAW Line " + line);

            FieldSet fieldSet = lineTokenizer.tokenize(line);

            for(int i=0;i<fieldSet.getFieldCount();i++){
                System.out.println("Field["+i+"] = ["+fieldSet.readString(i) + "]");
            }
            System.out.println("=======================");
            return fieldSetMapper.mapFieldSet(fieldSet);
        };
    }
}
