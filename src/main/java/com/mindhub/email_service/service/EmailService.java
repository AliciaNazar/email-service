package com.mindhub.email_service.service;


import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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
        String fileName = "order_" + event.getOrderId() + ".pdf";

        try (PdfWriter writer = new PdfWriter(fileName)) {
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            Paragraph title = new Paragraph("Order Confirmation")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .simulateBold();
            document.add(title);

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Order ID: " + event.getOrderId()).setFont(boldFont));
            document.add(new Paragraph("User Email: " + event.getCustomerEmail()).setFont(regularFont));
            document.add(new LineSeparator(new SolidLine()));
            document.add(new Paragraph("Items Ordered:").setFont(boldFont).setFontSize(14));
            float[] columnWidths = {100f, 200f, 100f}; // Con esto creo una tabla
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            table.addHeaderCell(new Cell().add(new Paragraph("Product ID").setFont(boldFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Name").setFont(boldFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Quantity").setFont(boldFont)));

            for (ProductDTO product : event.getProducts()) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getId())).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(product.getName()).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(product.getQuantity() != null ? product.getQuantity().toString() : "N/A").setFont(regularFont)));
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Thank you for your order!").setFont(boldFont).setFontSize(14));

            document.close();
            System.out.println("PDF generated: " + fileName);
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

}

