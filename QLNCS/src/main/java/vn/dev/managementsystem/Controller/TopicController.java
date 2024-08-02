package vn.dev.managementsystem.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.dev.managementsystem.Dto.TopicDto;
import vn.dev.managementsystem.Entity.Attachment;
import vn.dev.managementsystem.Entity.Group;
import vn.dev.managementsystem.Entity.Topic;
import vn.dev.managementsystem.Entity.User;
import vn.dev.managementsystem.Repository.GroupRepository;
import vn.dev.managementsystem.Service.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserService userService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private GroupService groupService;

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @GetMapping()
    public List<Topic> getAllTopics(){
        return topicService.getAllTopic();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTopicById(@PathVariable Integer id){
        Map<String, Object> jsonResult = new HashMap<>();
        Topic entity = topicService.getById(id);
        if(entity == null){
            jsonResult.put("Topic not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", entity);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @GetMapping("/order-by-state")
    public ResponseEntity<Map<String, List<Topic>>> getTopicsByState(){
        Map<String, List<Topic>> jsonResult = new HashMap<>();
        List<Topic> ts = topicService.orderByStatus();
        jsonResult.put("Successfully", ts);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @GetMapping("/order-by-lecturer")
    public ResponseEntity<Map<String, List<Topic>>> getTopicsByTeacher(){
        Map<String, List<Topic>> jsonResult = new HashMap<>();
        List<Topic> ts = topicService.orderByLecturer();
        jsonResult.put("Successfully", ts);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @GetMapping("/order-by-createdate")
    public ResponseEntity<Map<String, List<Topic>>> getTopicsByDate(){
        Map<String, List<Topic>> jsonResult = new HashMap<>();
        List<Topic> ts = topicService.orderByCreateDate();
        jsonResult.put("Successfully", ts);
        return ResponseEntity.ok(jsonResult);
    }

    //TODO-PRODUCE==========================================================
    @GetMapping(value = {"/{date}",
                         "/{state}",
                         })
    public List<Topic> getTopicsByKey(@PathVariable(value = "date", required = false) String date,
                                      @PathVariable(value = "state", required = false) String state){
        return null;
    }


    @PostMapping()
    public ResponseEntity<Map<String, Object>> createTopic(@RequestBody TopicDto topicDto){
        Map<String, Object> jsonResult = new HashMap<>();
        Topic model = topicService.saveAddTopic(topicDto);
        if(model == null){
            jsonResult.put("lecturer not found", "2044");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("successfully", model);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @GetMapping("/{email:.+}")
    public ResponseEntity<Map<String, Set<Topic>>> getLecturerTopics(@PathVariable String email){
        Map<String, Set<Topic>> jsonResult = new HashMap<>();
        if(userService.getUserByEmail(email) == null){
            System.out.println("EMAIL Null");
            jsonResult.put("email not found", new HashSet<>());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        Set<Topic> topicsOfLecturer = userService.getTopicsOfLecturer(email);
        System.out.println("EMAIL NOT Null: " + topicsOfLecturer.stream().findFirst());
        jsonResult.put("successfully", topicsOfLecturer);
        return ResponseEntity.ok(jsonResult);
    }

    @PostMapping(value = {"/{topicId}/{userId}", "/{topicId}/{email}"})
    public ResponseEntity<Map<String, Set<User>>> addStudentsForTopic(
                                            @PathVariable(value = "topicId") Integer topicId,
                                            @PathVariable(value = "userId", required = false) Integer userId,
                                            @PathVariable(value = "email", required = false) String email){
        Set<User> result = new HashSet<>();
        Map<String, Set<User>> jsonResult = new HashMap<>();
        if(email != null){
            result = topicService.addStudentForTopic(topicId, email);
            if (result == null){
                jsonResult.put("User or topic not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
            }
        }else if(userId!=null){
            result = topicService.addStudentForTopic(topicId,userId);
            if (result == null){
                jsonResult.put("User or topic not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
            }
        }else {
            jsonResult.put("Missing URI student's email or id", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);
        }
        jsonResult.put("Successfully", result);
        return ResponseEntity.ok(jsonResult);
    }

    @GetMapping("/students/{topicId}")
    public ResponseEntity<Map<String, Set<User>>> getStudentsOfTopic(@PathVariable Integer topicId){
        Map<String, Set<User>> jsonResult = new HashMap<>();
        if(topicService.getById(topicId) == null){
            jsonResult.put("Topic not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        Set<User> result = topicService.getById(topicId).getStudentsOfTopic();
        jsonResult.put("Successfully", result);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @PostMapping("/set-approve/{topicId}")
    public ResponseEntity<Map<String, Object>> setApproved(
            @PathVariable(value = "topicId", required = false) Integer topicId,
            @RequestParam(value = "email", required = false) String email){
        Map<String, Object> jsonResult = new HashMap<>();
        if(email == null){
                jsonResult.put("Missing email", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);

        }else if(topicId==null){
                jsonResult.put("Missing topic id", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);
        }
        Topic t = topicService.setToApprove(topicId, email);
        if(t == null){
            jsonResult.put("Topic or lecturer not found", "704");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", t);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @PostMapping("/set-under-review/{topicId}")
    public ResponseEntity<Map<String, Object>> setUnderRev(
            @PathVariable(value = "topicId", required = false) Integer topicId){
        Map<String, Object> jsonResult = new HashMap<>();
        if(topicId==null){
            jsonResult.put("Missing topic id", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);
        }
        Topic t = topicService.setToUnderReview(topicId);
        if(t == null){
            jsonResult.put("Topic not found", "704");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", t);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @PostMapping("/set-pause-or-resume/{topicId}")
    public ResponseEntity<Map<String, Object>> setPauseOrResume(
            @PathVariable(value = "topicId", required = false) Integer topicId){
        Map<String, Object> jsonResult = new HashMap<>();
        if(topicId==null){
            jsonResult.put("Missing topic id", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);
        }
        Topic t = topicService.setToPauseOrResume(topicId);
        if(t == null){
            jsonResult.put("Topic not found", "704");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", t);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @PostMapping("/set-cancel/{topicId}")
    public ResponseEntity<Map<String, Object>> setCancel(
            @PathVariable(value = "topicId", required = false) Integer topicId){
        Map<String, Object> jsonResult = new HashMap<>();
        if(topicId==null){
            jsonResult.put("Missing topic id", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);
        }
        Topic t = topicService.setToCancel(topicId);
        if(t == null){
            jsonResult.put("Topic not found", "704");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", t);
        return ResponseEntity.ok(jsonResult);
    }

    @PostMapping("/attachment/{topicId}")
    public ResponseEntity<Map<String, Attachment>> addAttachForReport(
            @PathVariable Integer topicId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "name", required = false) String name){
        Map<String, Attachment> jsonResult = new HashMap<>();
        if(file == null){
            jsonResult.put("Missing file param", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);
        }
        Attachment a = attachmentService.saveAddAttachmentOfTopic(topicId, name, file);
        if(a==null){
            jsonResult.put("topic not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", a);
        return ResponseEntity.status(HttpStatus.OK).body(jsonResult);
    }

    @GetMapping("/attachment/{topicId}")
    public ResponseEntity<Map<String, Set<Attachment>>> getAttachmentsOfReport(
            @PathVariable Integer topicId){
        Map<String, Set<Attachment>> jsonResult = new HashMap<>();
        Set<Attachment> a = attachmentService.getListByTopicId(topicId);
        if(a==null){
            jsonResult.put("topic not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", a);
        return ResponseEntity.status(HttpStatus.OK).body(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @PostMapping("/set-score/{topicId}")
    public ResponseEntity<Map<String, Object>> setScore(
            @PathVariable(value = "topicId", required = false) Integer topicId,
            @RequestParam(value = "score", required = false) String score){
        Map<String, Object> jsonResult = new HashMap<>();
        if(topicId==null){
            jsonResult.put("Missing topic id", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);
        }
        if(score==null){
            jsonResult.put("Missing score param", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResult);
        }
        Topic t = topicService.setToScore(topicId, score);
        if(t == null){
            jsonResult.put("Topic not found", "704");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", t);
        return ResponseEntity.ok(jsonResult);
    }

    @GetMapping("/council/{topicId}")
    public ResponseEntity<Map<String, Object>> getCouncil(@PathVariable Integer topicId){
        Map<String, Object> jsonResult = new HashMap<>();
        Group g = groupService.getGroupByTopicId(topicId);
        if (g == null){
            jsonResult.put("Topic not found", "704");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", g);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @PostMapping("/council/{topicId}")
    public ResponseEntity<Map<String, Object>> addCouncil(@PathVariable Integer topicId,
                                                          @RequestBody Group group){
        Map<String, Object> jsonResult = new HashMap<>();
        Group g = groupService.saveAddGroup(topicId, group);
        if (g == null){
            jsonResult.put("Topic not found", "704");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", g);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @PutMapping("/council/{topicId}")
    public ResponseEntity<Map<String, Object>> updateCouncil(@PathVariable Integer topicId,
                                                          @RequestBody Group group){
        Map<String, Object> jsonResult = new HashMap<>();
        Group g = groupService.saveUpdateGroup(topicId, group);
        if (g == null){
            jsonResult.put("Topic not found", "704");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        jsonResult.put("Successfully", g);
        return ResponseEntity.ok(jsonResult);
    }
}
