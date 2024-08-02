package vn.dev.managementsystem.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.dev.managementsystem.Service.MailReceiveService;

@RestController
@RequestMapping("/client")
public class ClientController {
    @Autowired
    private MailReceiveService clientService;

    @PostMapping(value = "create")
    public ResponseEntity<Boolean> create(
            @RequestParam("lecturerName") String lecturerName,
            @RequestParam("topicName") String topicName,
            @RequestParam("lecturerEmail") String lecturerEmail
    ) {
        return ResponseEntity.ok(clientService.createMailByStudent(lecturerEmail, lecturerName, topicName));
    }
}
