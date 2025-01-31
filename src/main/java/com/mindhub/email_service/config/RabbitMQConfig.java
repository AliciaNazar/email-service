package com.mindhub.email_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;


@Configuration
public class RabbitMQConfig {

    @Value("${MIEMAIL}")
    private String EMAIL;

    @Value("${MIPASSWORD}")
    private String MI_PASSWORD;

    public static final String QUEUE_NAME = "email-queue";
    public static final String QUEUE_PDF = "orderCreatedEvent";

    @Bean
    public Queue userRegistrationQueue() {
        return new Queue(QUEUE_NAME, true);
    }
    @Bean
    public Queue pdfQueue() {
        return new Queue(QUEUE_PDF, true);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange("email-exchange");
    }

    @Bean
    public Binding emailBinding(Queue userRegistrationQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userRegistrationQueue).to(userExchange).with("user.email");
    }

    @Bean
    public Binding pdfBinding(Queue pdfQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(pdfQueue).to(userExchange).with("user.pdf");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }


@Bean
public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    mailSender.setHost("smtp.gmail.com");
    mailSender.setPort(587);
    mailSender.setUsername(EMAIL);
    mailSender.setPassword(MI_PASSWORD);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "true");

    return mailSender;
}



}
