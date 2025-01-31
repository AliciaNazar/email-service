package com.mindhub.email_service.service;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.mindhub.email_service.config.RabbitMQConfig;
import com.mindhub.email_service.events.EmailEvent;
import com.mindhub.email_service.events.OrderToPdfDTO;
import com.mindhub.email_service.events.ProductDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${MIEMAIL}")
    private String EMAIL;

    @Autowired
    private TemplateEngine templateEngine;


    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void sendEmail(EmailEvent emailEvent) throws MessagingException {

        String username = emailEvent.getUsername();
        String confirmationLink = emailEvent.getBody();

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("confirmationLink", confirmationLink);
        String htmlContent = templateEngine.process("email-template", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(emailEvent.getTo());
        helper.setSubject(emailEvent.getSubject());
        helper.setText(htmlContent, true);

        mailSender.send(message);
        System.out.println("Correo enviado en HTML a: " + emailEvent.getTo());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PDF)
    public void sendPdfOrderEmail (OrderToPdfDTO orderDTO) throws MessagingException {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            generatePdfContent(contentStream, orderDTO, page);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            byte[] pdfBytes = byteArrayOutputStream.toByteArray();
            document.close();
            System.out.println("se creo el pdf");
            sendEmail(orderDTO.getUserMail(),pdfBytes,"document.pdf", orderDTO.getOrderId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generatePdfContent(PDPageContentStream contentStream, OrderToPdfDTO orderDTO, PDPage page) throws IOException {
        PDType1Font font = PDType1Font.HELVETICA_BOLD;
        contentStream.setFont(font, 14);
        contentStream.beginText();
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(64, 750);
        contentStream.showText("Order ID: "+orderDTO.getOrderId());
        contentStream.newLine();
        Double total = 0D;
        for (ProductDTO item : orderDTO.getnewProductList()){
            contentStream.showText("Product ID: " + item.getId() + " name: " + item.getName() + " description: " + item.getDescription()+" price: "+ item.getPrice()+  " Quantity: "+ item.getQuantity());
            total = total + item.getPrice()*item.getQuantity();
            contentStream.newLine();
        }
        contentStream.showText("Total: "+total);
        contentStream.endText();
        contentStream.close();
    }

    private void sendEmail(String to, byte[] pdfBytes, String fileName, Long orderId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("The order "+orderId+" was confirmed");
        helper.setText("Dear Customer,\n\nPlease find your order details attached.", false);

        helper.addAttachment(fileName, () -> new java.io.ByteArrayInputStream(pdfBytes));

        mailSender.send(message);
    }

}

