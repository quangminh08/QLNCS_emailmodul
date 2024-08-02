package vn.dev.managementsystem.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.dev.managementsystem.Dto.AuthenticationRequest;
import vn.dev.managementsystem.Dto.AuthenticationResponse;
import vn.dev.managementsystem.Dto.UserRequestDto;
import vn.dev.managementsystem.Entity.User;
import vn.dev.managementsystem.Service.AuthenticationService;
import vn.dev.managementsystem.Service.UserService;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession session;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
                        @RequestBody AuthenticationRequest authenticationRequest){

        Map<String, Object> jsonResult = new HashMap<>();
        boolean check = userService.checkExistEmail(authenticationRequest.getUsername());
        if (!check){
            jsonResult.put("could not login", "Not exactly email");
            return ResponseEntity.ok(jsonResult);
        }
        boolean checkP = userService.checkPassword(authenticationRequest.getUsername(),
                                                    authenticationRequest.getPassword());
        if (!checkP){
            jsonResult.put("could not login", "Not exactly password");
            return ResponseEntity.ok(jsonResult);
        }
        session.setAttribute("username", authenticationRequest.getUsername());
        AuthenticationResponse model = authenticationService.authenticate(authenticationRequest);
        jsonResult.put("successfully", model);
        return ResponseEntity.ok(jsonResult);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody UserRequestDto requestUser){
        Map<String, Object> jsonResult = new HashMap<>();
        boolean check = userService.checkExistEmail(requestUser.getEmail());
        if (check){
            jsonResult.put("could not register", "email already exist");
            return ResponseEntity.ok(jsonResult);
        }
        boolean c = userService.saveAddStudent(requestUser);
        if (!c){
            jsonResult.put("could not register", "invalid password");
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(jsonResult);
        }
        User model = userService.getUserByEmail(requestUser.getEmail());
        jsonResult.put("successfully", model);
        return ResponseEntity.ok(jsonResult);
    }
}
