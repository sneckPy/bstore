package com.bstore.user.service;

import com.bstore.commons.exception.DataRetrievalException;
import com.bstore.commons.exception.EmailAlreadyUsedException;
import com.bstore.commons.model.request.UserRequest;
import com.bstore.commons.model.response.UserDetailsResponse;
import com.bstore.commons.model.response.UserResponse;
import com.bstore.user.model.entity.User;
import com.bstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ConversionService conversionService;

    public UserResponse register(UserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) throw new EmailAlreadyUsedException(dto.getEmail());
        User user = userRepository.save(Objects.requireNonNull(conversionService.convert(dto, User.class)));
        return conversionService.convert(user, UserResponse.class);
    }

    public UserResponse update(UserRequest dto) {
        User user = userRepository.save(Objects.requireNonNull(conversionService.convert(dto, User.class)));
        return conversionService.convert(user, UserResponse.class);
    }

    public List<UserResponse> search(UserRequest filter) {
        User user = new User();
        user.setId(filter.getId());
        user.setFirstName(filter.getFirstName());
        user.setLastName(filter.getLastName());
        user.setEmail(filter.getEmail());
        user.setShippingAddress(filter.getShippingAddress());
        user.setBirthDate(filter.getBirthDate());

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();
        Example<User> example = Example.of(user, matcher);

        return userRepository.findAll(example).stream().map(data -> conversionService.convert(data, UserResponse.class)).toList();
    }

    public UserDetailsResponse findDetailsByEmail(String email) {
        User userEntity = userRepository.findByEmail(email).orElseThrow(() -> new DataRetrievalException("User not found"));
        return conversionService.convert(userEntity, UserDetailsResponse.class);
    }

    public List<UserResponse> get() {
        return userRepository.findAll().stream().map(user -> conversionService.convert(user, UserResponse.class)).toList();
    }

    public UserDetailsResponse findDetailsById(Long id) {
        User userEntity = userRepository.findById(id).orElseThrow(() -> new DataRetrievalException("User not found"));
        return conversionService.convert(userEntity, UserDetailsResponse.class);
    }
}
