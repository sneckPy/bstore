package com.bstore.user.controller;

import com.bstore.commons.model.request.UserRequest;
import com.bstore.commons.model.response.UserDetailsResponse;
import com.bstore.commons.model.response.UserResponse;
import com.bstore.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> searchUsers(@ModelAttribute UserRequest filter) {
        List<UserResponse> userList = userService.search(filter);
        if (userList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.status(HttpStatus.OK).body(userList);
    }

    @GetMapping("/details")
    public ResponseEntity<UserDetailsResponse> findDetailsByEmail(@RequestParam("email") String email) {
        UserDetailsResponse response = userService.findDetailsByEmail(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.update(request));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsResponse> getById(@PathVariable Long id) {
        UserDetailsResponse user = userService.findDetailsById(id);
        return ResponseEntity.ok(user);
    }

}
