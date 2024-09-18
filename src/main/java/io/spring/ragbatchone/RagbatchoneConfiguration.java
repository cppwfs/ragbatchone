package io.spring.ragbatchone;

import io.spring.aibatchtools.TikaItemReaderBuilder;
import io.spring.aibatchtools.VectorStoreWriterBuilder;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
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
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableTask
public class RagbatchoneConfiguration {

    @Value("classpath:/docs/depdoc.pdf")
    private Resource textResource;

    @Bean
    public ItemReader reader() {
        return new TikaItemReaderBuilder().name("PDFReader").
                textSplitter(new TokenTextSplitter()).
                resource(textResource).
                build();
    }

    @Bean
    public ItemWriter writer(VectorStore vectorStore) {
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
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, VectorStore vectorStore) {
        return new StepBuilder("step1", jobRepository)
                .chunk(10, transactionManager)
                .reader(reader())
                .writer(writer(vectorStore))
                .build();
    }
}
