package com.mindhub.email_service.service;


import com.mindhub.email_service.config.RabbitMQConfig;
import com.mindhub.email_service.events.EmailEvent;
import com.mindhub.email_service.events.OrderCreatedEvent;
import com.mindhub.email_service.events.ProductDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${MIEMAIL}")
    private String EMAIL;


    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void sendEmail(EmailEvent emailEvent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailEvent.getTo());
        message.setSubject(emailEvent.getSubject());
        message.setText(emailEvent.getBody());
        mailSender.send(message);

        System.out.println("Correo enviado a: " + emailEvent.getTo());
    }


    @RabbitListener(queues = RabbitMQConfig.QUEUE_PDF)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        generateOrderPdf(event); //genero el pdf con los detalles del pedido
        sendEmailWithPdf(event);
    }

    private void generateOrderPdf(OrderCreatedEvent event) {
        try (PdfWriter writer = new PdfWriter("order_" + event.getOrderId() + ".pdf")) {
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            // aquí voy poniendo la estructura del documento que voy a mandar
            document.add(new Paragraph("Order Details").setFontSize(25));
            document.add(new Paragraph("Order ID: " + event.getOrderId()));
            document.add(new Paragraph("User email: " + event.getCustomerEmail()));
            document.add(new Paragraph("Items:"));

            for (ProductDTO product : event.getProducts()) {
                document.add(new Paragraph("- Product ID: " + product.getId() +
                        ", Name: " + product.getName() +
                        ", Quantity: " + (product.getQuantity() != null ? product.getQuantity() : "N/A")));
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEmailWithPdf(OrderCreatedEvent event) {
        String subject = "Order Confirmation - Order ID: " + event.getOrderId();
        String body = "This is an order confirmation for the Order ID: " + event.getOrderId()+".  You will find the details on the following pdf.";
        String pdfFilePath = "order_" + event.getOrderId() + ".pdf";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(event.getCustomerEmail()); // este es a quien le quiero mandar
            helper.setSubject(subject);
            helper.setText(body);

            File pdfFile = new File(pdfFilePath);
            helper.addAttachment(pdfFile.getName(), pdfFile);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

















//    @RabbitListener(queues = "order.created.queue")
//    public void processOrderCreatedEvent(OrderCreatedEvent event) {
//        // Generar el PDF y enviar el email
//        generateAndSendPDF(event);
//    }
//
//    private void generateAndSendPDF(OrderCreatedEvent event) {
//        String pdfPath = generatePDF(event);
//
//        SimpleMailMessage email = new SimpleMailMessage();
//        email.setTo(event.getCustomerEmail());
//        email.setSubject("Detalles de tu orden: " + event.getOrderId());
//        email.setText("Adjuntamos los detalles de tu orden.");
//
//        // Enviar email con el PDF adjunto
//        sendEmailWithAttachment(email, pdfPath);
//    }
//
//    private String generatePDF(OrderCreatedEvent event) {
//        // Usa una librería como iText o Apache PDFBox para generar el PDF
//        String filePath = "/path/to/pdf/" + event.getOrderId() + ".pdf";
//        try (OutputStream outputStream = new FileOutputStream(filePath)) {
//            Document document = new Document();
//            PdfWriter.getInstance(document, outputStream);
//
//            document.open();
//            document.add(new Paragraph("Detalles de la Orden"));
//            document.add(new Paragraph("Orden ID: " + event.getOrderId()));
//
//            document.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return filePath;
//    }
//
//    private void sendEmailWithAttachment(SimpleMailMessage email, String filePath) {
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        try {
//            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
//            helper.setTo(email.getTo());
//            helper.setSubject(email.getSubject());
//            helper.setText(email.getText());
//            helper.addAttachment("DetallesOrden.pdf", new File(filePath));
//
//            mailSender.send(mimeMessage);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}

