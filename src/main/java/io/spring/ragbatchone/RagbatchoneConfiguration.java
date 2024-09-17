package io.spring.ragbatchone;

import io.spring.aibatchtools.TikaItemReader;
import io.spring.aibatchtools.VectorStoreWriterBuilder;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RagbatchoneConfiguration {

    @Value("classpath:/docs/batchtalk.txt")
    private Resource textResource;

    @Bean
    public ItemReader reader() {
        TikaItemReader tikaItemReader = new TikaItemReader();
        tikaItemReader.setResource(new FileSystemResource("/Users/grenfro/Downloads/scdfguide.pdf"));
        return tikaItemReader;
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
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, ItemWriter writer) {
        return new StepBuilder("step1", jobRepository)
                .<Instruction, Instruction>chunk(10, transactionManager)
                .reader(reader())
                .writer(writer)
                .build();
    }
}
