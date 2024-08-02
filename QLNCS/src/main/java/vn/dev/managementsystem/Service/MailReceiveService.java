package vn.dev.managementsystem.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import vn.dev.managementsystem.Dto.DataMailDTO;
import vn.dev.managementsystem.utils.Const;
import vn.dev.managementsystem.utils.DataUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class MailReceiveService {
    @Autowired
    private MailSendService mailSendService;

    public Boolean createMailByStudent(String lecturerEmail, String lecturerName, String topicName) {
        try {
            DataMailDTO dataMail = new DataMailDTO();

            dataMail.setTo(lecturerEmail);
            dataMail.setSubject(Const.SEND_MAIL_SUBJECT.STUDENT_REPORT_REGISTER);

            Map<String, Object> props = new HashMap<>();
            props.put("lecturerName", lecturerName);
            props.put("topicName", topicName);
            props.put("code", DataUtils.generateTempPwd(6));
            dataMail.setProps(props);

            mailSendService.sendHtmlMail(dataMail, Const.TEMPLATE_FILE_NAME.STUDENT_REGISTER_TEMPLATE);
            return true;
        } catch (MessagingException exp){
            exp.printStackTrace();
        }
        return false;
    }

    public Boolean createMailByLecturer(String studentEmail, String studentName, String topicName) {
        try {
            DataMailDTO dataMail = new DataMailDTO();

            dataMail.setTo(studentEmail);
            dataMail.setSubject(Const.SEND_MAIL_SUBJECT.LECTURER_REPORT_REGISTER);

            Map<String, Object> props = new HashMap<>();
            props.put("studentName", studentName);
            props.put("topicName", topicName);
            props.put("code", DataUtils.generateTempPwd(6));
            dataMail.setProps(props);

            mailSendService.sendHtmlMail(dataMail, Const.TEMPLATE_FILE_NAME.LECTURER_REGISTER_TEMPLATE);
            return true;
        } catch (MessagingException exp){
            exp.printStackTrace();
        }
        return false;
    }
}
