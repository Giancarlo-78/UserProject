package com.gdellecese.userproject.mapper;

import com.gdellecese.userproject.dto.UserRequestDto;
import com.gdellecese.userproject.dto.UserResponseDto;
import com.gdellecese.userproject.model.User;

public class UserMapper {

    public static User toEntity(UserRequestDto dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .build();
    }

    public static UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .address(user.getAddress())
                .build();
    }
}
