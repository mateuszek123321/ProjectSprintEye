package org.example.serversprinteye.services;


import jakarta.mail.internet.MimeMessage;
import org.example.serversprinteye.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class EmailVerificationService {
    private JavaMailSender mailSender;

    @Value("${spring.mail.username")
    private String from;

    public void sendVerificationEmail(String email, String verificationToken) {
        String subject = "Verification Email";
        String path = "/request/register/verifiy";
        String message = "Kliknij poniżej aby zweryfikować email";
        sendEmail(email, verificationToken, subject, path, message);
    }

    public void sendForgotPasswordEmail(String email, String resetToken) {
        String subject = "Forgotten Password";
        String path = "/request/reset-password";
        String message = "Kliknij poniżej aby zresetować hasło";
        sendEmail(email, resetToken, subject, path, message);
    }

    private void sendEmail(String email, String token, String subject, String path, String message) {
        try{
            String actionUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(path)
                    .queryParam("token", token)
                    .toUriString();

            String content = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background-color: #f4f4f4;
                                margin: 0;
                                padding: 0;
                            }
                            .container {
                                max-width: 600px;
                                margin: 20px auto;
                                background-color: #ffffff;
                                padding: 20px;
                                border-radius: 10px;
                                box-shadow: 0 0 10px rgba(0,0,0,0.1);
                            }
                            h1 {
                                color: #333333;
                                text-align: center;
                            }
                            p {
                                color: #666666;
                                font-size: 16px;
                                line-height: 1.6;
                            }
                            .button {
                                display: inline-block;
                                padding: 15px 30px;
                                margin: 20px 0;
                                background-color: #4CAF50;
                                color: white !important;
                                text-decoration: none;
                                border-radius: 5px;
                                font-weight: bold;
                                text-align: center;
                            }
                            .button:hover {
                                background-color: #45a049;
                            }
                            .footer {
                                text-align: center;
                                margin-top: 20px;
                                font-size: 12px;
                                color: #999999;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>%s</h1>
                            <p>%s</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">Kliknij tutaj</a>
                            </div>
                            <p class="footer">To jest automatyczna wiadomość. Prosimy nie odpowiadać na ten email.</p>
                        </div>
                    </body>
                    </html>
                    """, subject, message, actionUrl);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(content, true);
            mailSender.send(mimeMessage);

        }catch (Exception e){
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

}
