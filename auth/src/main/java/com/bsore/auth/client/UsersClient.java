package com.bsore.auth.client;


import com.bstore.commons.model.request.UserRequest;
import com.bstore.commons.model.response.UserDetailsResponse;
import com.bstore.commons.model.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "users-client", url = "${gateway.url}/users")
public interface UsersClient {
    @GetMapping("/details")
    UserDetailsResponse getByEmail(@RequestParam("email") String email);

    @GetMapping("/{id}")
    UserDetailsResponse getByEmailById(@PathVariable("id") Long id);

    @PostMapping("/{id}")
    UserResponse registerUser(@Valid @RequestBody UserRequest request);

    @DeleteMapping("/{id}")
    UserResponse deleteUser(@PathVariable Long id);
}