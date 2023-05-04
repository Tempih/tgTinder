package ru.liga.serverfortgtinder.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
        String userId;
        String userIdProfile;
        Integer chatId;
        @NotBlank
        String gender;
        @NotBlank
        String name;
        String header;
        String description;
        String searchGender;
        String photo;
        int currentProfileId;
        int howLiked;
}
