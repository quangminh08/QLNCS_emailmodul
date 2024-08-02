package vn.dev.managementsystem.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.dev.managementsystem.Dto.AttachmentDto;
import vn.dev.managementsystem.Entity.Attachment;
import vn.dev.managementsystem.Entity.Report;
import vn.dev.managementsystem.Entity.Topic;
import vn.dev.managementsystem.Entity.User;
import vn.dev.managementsystem.Repository.AttachmentRepository;
import vn.dev.managementsystem.Repository.ReportRepository;
import vn.dev.managementsystem.Repository.TopicRepository;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Service
public class AttachmentService {

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private  CloudinaryService cloudinaryService;

    public Attachment saveAddAttachmentOfReport(Integer reportId, String name, MultipartFile file){
        Report r = reportRepository.findById(reportId).orElse(null);
        if(r == null){
            return null;
        }
        Map data = this.cloudinaryService.upload(file);
        Attachment a = new Attachment();
        a.setName(name);
        a.setPath(data.get("url").toString());
        a.setCreate_date(new Date());
        a.setReportAttachment(r);
        return attachmentRepository.save(a);
    }

    public Set<Attachment> getListByReportId(Integer reportId){
        Report r = reportRepository.findById(reportId).orElse(null);
        if(r == null){
            return null;
        }
        return r.getAttachments();
    }


    public Attachment saveAddAttachmentOfTopic(Integer topicId, String name, MultipartFile file){
        Topic r = topicRepository.findById(topicId).orElse(null);
        if(r == null){
            return null;
        }
        Map data = this.cloudinaryService.upload(file);
        Attachment a = new Attachment();
        a.setName(name);
        a.setPath(data.get("url").toString());
        a.setCreate_date(new Date());
        a.setTopicAttachment(r);
        return attachmentRepository.save(a);
    }

    public Set<Attachment> getListByTopicId(Integer topicId){
        Topic r = topicRepository.findById(topicId).orElse(null);
        if(r == null){
            return null;
        }
        return r.getAttachments();
    }

}
