package vn.dev.managementsystem.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.dev.managementsystem.Dto.UserRequestDto;
import vn.dev.managementsystem.Entity.Topic;
import vn.dev.managementsystem.Entity.User;
import vn.dev.managementsystem.Service.CloudinaryService;
import vn.dev.managementsystem.Service.UserService;

import javax.swing.text.html.parser.Entity;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private HttpSession session;


    @PreAuthorize("hasAuthority('admin')")
    @PostMapping(value = "/create-admin")
    public ResponseEntity<Map<String, Object>> createAdmin(
            @RequestBody UserRequestDto requestUser){
        Map<String, Object> jsonResult = new HashMap<>();
        boolean check = userService.checkExistEmail(requestUser.getEmail());
        if (check){
            jsonResult.put("could not register", "email already exist");
            return ResponseEntity.ok(jsonResult);
        }
        boolean c = userService.saveAddAdmin(requestUser);
        if (!c){
            jsonResult.put("could not register", "invalid password");
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(jsonResult);
        }
        User model = userService.getUserByEmail(requestUser.getEmail());
        jsonResult.put("successfully", model);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping(value = "/create-lecturer")
    public ResponseEntity<Map<String, Object>> createLecturer(
            @RequestBody UserRequestDto requestUser){
        Map<String, Object> jsonResult = new HashMap<>();
        boolean check = userService.checkExistEmail(requestUser.getEmail());
        if (check){
            jsonResult.put("could not register", "email already exist");
            return ResponseEntity.ok(jsonResult);
        }
        boolean c = userService.saveAddLecturer(requestUser);
        if (!c){
            jsonResult.put("could not register", "invalid password");
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(jsonResult);
        }
        User model = userService.getUserByEmail(requestUser.getEmail());
        jsonResult.put("successfully", model);
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping(value = {"", "/{role}"})
    public ResponseEntity<Map<String, List<User>>> getUserList(@PathVariable(value = "role", required = false) String role){
        Map<String, List<User>> jsonResult = new HashMap<>();
        if(role == null){
            List<User> models = userService.getAllUsers();
            jsonResult.put("successfully", models);
            return ResponseEntity.ok(jsonResult);
        }
        else if (role.equals("admin" )|| role.equals("lecturer") || role.equals("student")){
            List<User> models = userService.getUserListByRoleName(role);
            jsonResult.put("successfully", models);
            return ResponseEntity.ok(jsonResult);
        } else {
            jsonResult.put("[PathVariable] role name not exactly", new ArrayList<>());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile(){
        String email = (String) session.getAttribute("username");
        User model = userService.getUserByEmail(email);
        Map<String, Object> jsonResult = new HashMap<>();
        if(model != null){
            jsonResult.put("successfully", model);
        }else {
            jsonResult.put("User not found", "204");
        }
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @GetMapping("/get/{email:.+}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String email){
        User model = userService.getUserByEmail(email);
        Map<String, Object> jsonResult = new HashMap<>();
        if(model != null){
            jsonResult.put("successfully", model);
        }else {
            jsonResult.put("User not found", "204");
        }
        return ResponseEntity.ok(jsonResult);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping(value = {"/change-status/{email:.+}", "/delete/{email:.+}"})
    public ResponseEntity<String> inactiveUser(@PathVariable String email){
        boolean execute = userService.changeStatus(email);
        if(execute){
            return ResponseEntity.ok("successfully");
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }


    @PostMapping("/upload-avatar/{email:.+}")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file, @PathVariable String email){
        Map data = this.cloudinaryService.upload(file);
        User model = userService.uploadAvatar(data.get("url").toString(), email);
        Map<String, Object> jsonResult = new HashMap<>();
        if(model != null){
            jsonResult.put("successfully", model);
        }else {
            jsonResult.put("User not found", "204");
        }
        return ResponseEntity.ok(jsonResult);
    }

    @PostMapping("/set-password/{email:.+}")
    public  ResponseEntity<String> setPassword(@PathVariable String email, @RequestParam String password){
        if(password == null || password.trim().isEmpty()){
            return ResponseEntity.ok("Password is empty");
        }
        boolean execute = userService.setPassword(email, password.trim());
        if(execute){
            return ResponseEntity.ok("successfully");
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/put/{email:.+}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String email, @RequestBody UserRequestDto dto){
        User model = userService.update(email, dto);
        Map<String, Object> jsonResult = new HashMap<>();
        if(model != null){
            jsonResult.put("successfully", model);
        }else {
            jsonResult.put("User not found", "204");
        }
        return ResponseEntity.ok(jsonResult);
    }


    @GetMapping("/export/{role}")
    public ResponseEntity<Resource> exportCsv(@PathVariable String role) throws IOException {
        String fileName = "export"+ role +".csv";
        if (!role.equals("admin") && !role.equals("lecturer") && !role.equals("student")) {
            return ResponseEntity.ofNullable(null);
        }
        List<User> users = userService.getUserListByRoleName(role);
        System.out.println("LIST SIZE: " + users.size());
        ByteArrayInputStream fileData = userService.generateCsv(users);
        InputStreamResource resource = new InputStreamResource(fileData);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; fileName="+fileName)
                .contentType(MediaType.parseMediaType("application/csv")).body(resource);

    }

    @PreAuthorize("hasAnyAuthority('lecturer', 'admin')")
    @GetMapping("/topics/{email:.+}")
    public ResponseEntity<Map<String, Set<Topic>>> getLecturerTopics(@PathVariable String email){
        Map<String, Set<Topic>> jsonResult = new HashMap<>();
        if(userService.getUserByEmail(email) == null){
            jsonResult.put("email not found", new HashSet<>());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonResult);
        }
        Set<Topic> topicsOfLecturer = userService.getTopicsOfLecturer(email);
        jsonResult.put("successfully", topicsOfLecturer);
        return ResponseEntity.ok(jsonResult);
    }
}
