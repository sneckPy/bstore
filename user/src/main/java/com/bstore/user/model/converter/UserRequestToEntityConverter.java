package com.bstore.user.model.converter;

import com.bstore.user.model.entity.User;
import com.bstore.commons.model.request.UserRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserRequestToEntityConverter
        implements Converter<UserRequest, User> {

    private final PasswordEncoder passwordEncoder;

    public UserRequestToEntityConverter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User convert(UserRequest source) {
        return User.builder()
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .email(source.getEmail())
                .shippingAddress(source.getShippingAddress())
                .birthDate(source.getBirthDate())
                .password(passwordEncoder.encode(source.getPassword()))
                .build();
    }
}
