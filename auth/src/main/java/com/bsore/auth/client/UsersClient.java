package com.bsore.auth.client;


import com.bstore.commons.model.response.UserDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "users-client", url = "${gateway.url}/users")
public interface UsersClient {
    @GetMapping("/details")
    UserDetailsResponse getByEmail(@RequestParam("email") String email);

    @GetMapping("/{id}")
    UserDetailsResponse getByEmailById(@PathVariable("id") Long id);
}