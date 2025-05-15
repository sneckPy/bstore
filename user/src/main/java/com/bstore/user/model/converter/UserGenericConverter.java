package com.bstore.user.model.converter;

import com.bstore.commons.model.response.UserDetailsResponse;
import com.bstore.user.model.entity.User;
import com.bstore.commons.model.request.UserRequest;
import com.bstore.commons.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserGenericConverter implements GenericConverter {

    private final UserRequestToEntityConverter userRequestToEntityConverter;
    private final EntityToUserResponseConverter entityToUserResponseConverter;
    private final EntityToUserDetailsResponseConverter entityToUserDetailsResponseConverter;


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(UserRequest.class, User.class), new ConvertiblePair(User.class, UserResponse.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (sourceType.getType() == UserRequest.class && targetType.getType() == User.class) {
            return userRequestToEntityConverter.convert((UserRequest) source);
        }
        if (sourceType.getType() == User.class && targetType.getType() == UserResponse.class) {
            return entityToUserResponseConverter.convert((User) source);
        }
        if (sourceType.getType() == User.class && targetType.getType() == UserDetailsResponse.class) {
            return entityToUserDetailsResponseConverter.convert((User) source);
        }
        throw new IllegalArgumentException("User converter not mapped");
    }
}
