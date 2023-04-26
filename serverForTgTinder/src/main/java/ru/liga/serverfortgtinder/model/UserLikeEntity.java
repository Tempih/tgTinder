package ru.liga.serverfortgtinder.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLikeEntity {
    @NotBlank
    String userId;
    Integer chatId;
    @NotBlank
    String likedUserId;
}
