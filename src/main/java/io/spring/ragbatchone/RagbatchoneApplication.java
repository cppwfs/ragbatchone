package io.spring.ragbatchone;

import java.util.Collections;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RagbatchoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagbatchoneApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner() {
		return new ApplicationRunner() {
			@Override
			public void run(ApplicationArguments args) throws Exception {
				PriceInformation priceInformation = new PriceInformation(.15D, "HelloWorld", "hello=yall");
				BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(priceInformation);
				System.out.println("interest =>" + beanWrapper.getPropertyValue("interest"));
				System.out.println("message =>" + beanWrapper.getPropertyValue("message"));
				System.out.println("myData =>" + beanWrapper.getPropertyValue("myData"));
			}
		};
	}

}
