package io.spring.ragbatchone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import io.spring.aibatchtools.VectorStoreWriterBuilder;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RagbatchoneConfiguration {

    @Value("classpath:/docs/batchtalk.txt")
    private Resource textResource;

    private Map<String, Object> metadata = new HashMap<>();

    @Bean
    public FlatFileItemReader <Instruction> reader(LineMapper <Instruction> lineMapper) {
        return new FlatFileItemReaderBuilder().name("batchTalkReader")
                .resource(textResource)
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    public ItemWriter<Instruction> writer(VectorStore vectorStore) {
        return new VectorStoreWriterBuilder().
                contentFieldName("message").
                metaDataFieldName("keyData").
                vectorStore(vectorStore).build();
    }

    @Bean
    public Job processDocumentsJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("importDocJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, ItemWriter writer, LineMapper lineMapper) {
        return new StepBuilder("step1", jobRepository)
                .<Instruction, Instruction> chunk(10, transactionManager)
                .listener(new MyListener(metadata))
                .reader(reader(lineMapper))
                .processor(processor(metadata))
                .writer(writer)
                .build();
    }

    @Bean
    public ItemProcessor<Instruction, Instruction> processor(Map<String, Object> metadata) {

        return new ItemProcessor<Instruction, Instruction>() {
            @Override
            public Instruction process(Instruction instruction) throws Exception {
                metadata.forEach(new BiConsumer<String, Object>() {
                    @Override
                    public void accept(String s, Object s2) {
                        instruction.getKeyData().put(s, s2.toString());
                    }
                });
                return instruction;
            }
        };
    }

    @Bean
    LineMapper<Instruction> lineMapper() {
        return new LineMapper<Instruction>() {
            @Override
            public Instruction mapLine(String line, int lineNumber) throws Exception {
                Instruction instruction = new Instruction();
                instruction.setMessage(line);
                return instruction ;
            }
        };
    }

    private class MyListener implements StepExecutionListener {

        Map<String, Object> metadata;
        public MyListener(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
        @Override
        public void beforeStep(StepExecution stepExecution) {
            JobParameters parameters = stepExecution.getJobExecution().getJobParameters();
            System.out.println("ENTERING BEFORE STEP");
            parameters.getParameters().forEach(new BiConsumer<String, JobParameter<?>>() {
                @Override
                public void accept(String s, JobParameter<?> jobParameter) {
                    metadata.put(s, jobParameter.getValue());
                }
            });

        }

    }
}
