package com.bstore.user.model.converter;

import com.bstore.commons.model.response.UserDetailsResponse;
import com.bstore.user.model.entity.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EntityToUserDetailsResponseConverter implements Converter<User, UserDetailsResponse> {
    @Override
    public UserDetailsResponse convert(User source) {
        return UserDetailsResponse.builder()
                .id(source.getId())
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .email(source.getEmail())
                .shippingAddress(source.getShippingAddress())
                .birthDate(source.getBirthDate())
                .passwordHash(source.getPassword())
                .build();
    }
}
