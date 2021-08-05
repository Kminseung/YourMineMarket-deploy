package com.bit.yourmine.service;

import com.bit.yourmine.config.MailHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSenderImpl javaMailSender;

    public void mailSend(HttpSession session, String userEmail) {
        try {
            MailHandler mailHandler = new MailHandler(javaMailSender);

            int code = (int) (Math.random()*100000);
            mailHandler.setToemail(userEmail);
            mailHandler.setFrom("yourmineService@gmail.com");
            mailHandler.setTitle("YourMine Service Web 인증번호입니다");
            mailHandler.setText("<p> 인증번호 : "+ code + "</p>", true);
            mailHandler.send();

            session.setAttribute("emailCodeInfo", code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean emailAuth(HttpSession session, int inputCode) {
        try {
            int collectCode = (int) session.getAttribute("emailCodeInfo");
            if (collectCode == inputCode) {
                session.removeAttribute("emailCodeInfo");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("MailService - emailAuth");
        }
        return false;
    }

    public void delMailSession(HttpSession session) { session.removeAttribute("emailCodeInfo"); }
}
