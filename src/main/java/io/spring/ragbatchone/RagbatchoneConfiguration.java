package io.spring.ragbatchone;

import io.spring.aibatchtools.VectorStoreWriterBuilder;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RagbatchoneConfiguration {

    @Value("classpath:/docs/pricing.txt")
    private Resource textResource;

    @Bean
    public FlatFileItemReader <PriceInformation> reader() {
        BeanWrapperFieldSetMapper beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper();
        beanWrapperFieldSetMapper.setTargetType(PriceInformation.class);
        return new FlatFileItemReaderBuilder().name("coffeeItemReader")
                .resource(textResource)
                .delimited()
                .quoteCharacter('|')
                .names(new String[] { "myData", "message"})
                .fieldSetMapper(beanWrapperFieldSetMapper)
                .build();
    }

    @Bean
    public ItemWriter<PriceInformation> writer(VectorStore vectorStore) {
        return new VectorStoreWriterBuilder().
                contentFieldName("message").
                metaDataFieldName("keyData").
                vectorStore(vectorStore).build();
    }

    @Bean
    public Job processDocumentsJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("importUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, ItemWriter writer) {
        return new StepBuilder("step1", jobRepository)
                .<PriceInformation, PriceInformation> chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    public ItemProcessor<PriceInformation, PriceInformation> processor() {

        return new ItemProcessor<PriceInformation, PriceInformation>() {
            @Override
            public PriceInformation process(PriceInformation priceInformation) throws Exception {
                priceInformation.getKeyData().put("Well Hello", "There");
                return priceInformation;
            }
        };
    }
}
