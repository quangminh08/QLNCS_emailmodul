package vn.dev.managementsystem.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.dev.managementsystem.Dto.ReportDto;
import vn.dev.managementsystem.Entity.Attachment;
import vn.dev.managementsystem.Entity.Report;
import vn.dev.managementsystem.Service.AttachmentService;
import vn.dev.managementsystem.Service.ReportService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AttachmentService attachmentService;

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<Map<String, Set<Report>>> getReportByTopicId(@PathVariable Integer topicId){
        Map<String, Set<Report>> jsonResult = new HashMap<>();
        Set<Report> rs = reportService.getReportsOfTopic(topicId);
        if(rs==null){
            jsonResult.put("Topic not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", rs);
        return ResponseEntity.ok(jsonResult);
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<Map<String, Report>> getReportById(@PathVariable Integer reportId){
        Map<String, Report> jsonResult = new HashMap<>();
        Report rs = reportService.getById(reportId);
        if(rs==null){
            jsonResult.put("Topic not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", rs);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'student')")
    @PostMapping("/topic/{topicId}")
    public ResponseEntity<Map<String, Report>> addReportByTopicId(@PathVariable Integer topicId,
                                                    @RequestBody ReportDto dto){
        Map<String, Report> jsonResult = new HashMap<>();
        Report rs = reportService.saveAddReport(dto);
        if(rs==null){
            jsonResult.put("Topic not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", rs);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'student')")
    @PostMapping("/attachment/{reportId}")
    public ResponseEntity<Map<String, Attachment>> addAttachForReport(
            @PathVariable Integer reportId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "name", required = false) String name){
        Map<String, Attachment> jsonResult = new HashMap<>();
        if(file == null){
            jsonResult.put("Missing file param", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);
        }
        Attachment a = attachmentService.saveAddAttachmentOfReport(reportId, name, file);
        if(a==null){
            jsonResult.put("Report not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", a);
        return ResponseEntity.status(HttpStatus.OK).body(jsonResult);
    }

    @GetMapping("/attachment/{reportId}")
    public ResponseEntity<Map<String, Set<Attachment>>> getAttachmentsForReport(
            @PathVariable Integer reportId){
        Map<String, Set<Attachment>> jsonResult = new HashMap<>();
        Set<Attachment> a = attachmentService.getListByReportId(reportId);
        if(a==null){
            jsonResult.put("Report not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        System.out.println("=============AIS" + a.size());
        jsonResult.put("Successfully", a);
        return ResponseEntity.status(HttpStatus.OK).body(jsonResult);
    }
}
