package com.bstore.user.model.converter;

import com.bstore.user.model.entity.User;
import com.bstore.commons.model.response.UserResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EntityToUserResponseConverter implements Converter<User, UserResponse> {
    @Override
    public UserResponse convert(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setEmail(user.getEmail());
        resp.setShippingAddress(user.getShippingAddress());
        resp.setBirthDate(user.getBirthDate());
        return resp;
    }
}
