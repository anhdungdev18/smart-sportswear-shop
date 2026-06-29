package com.dunghaiquyen.ecommerce.modules.user.mapper;

import com.dunghaiquyen.ecommerce.modules.user.dto.UserResponse;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
