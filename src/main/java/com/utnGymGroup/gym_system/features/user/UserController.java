package com.utnGymGroup.gym_system.features.user;

import com.utnGymGroup.gym_system.common.interfaces.ICreate;
import com.utnGymGroup.gym_system.common.interfaces.IUpdate;
import com.utnGymGroup.gym_system.features.user.dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponseDTO> getByUsername(@PathVariable String username){
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Validated(ICreate.class)
            @RequestBody UserCreateRequestDTO request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.userRegister(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid
            @RequestBody LoginRequestDTO request
    ){
        return ResponseEntity.ok(userService.login(request));
    }

    @PutMapping("/{username}")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @PathVariable String username,
            @Validated(IUpdate.class)
            @RequestBody UserUpdateDTO request
    ){
        return ResponseEntity.ok(userService.updateUser(username, request));
    }

    @PatchMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Validated(IUpdate.class)
            @RequestBody PasswordChangeDTO request
    ){
        userService.changePassword(username, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> toggleStatus(
            @PathVariable String username,
            @RequestParam boolean enabled
    ){
        userService.toggleUserStatus(username, enabled);
        return ResponseEntity.ok().build();
    }


}
