package ru.liga.serverfortgtinder.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    String userId;
    Integer chatId;
    @NotBlank
    String gender;
    @NotBlank
    String name;
    String header;
    String description;
    @NotBlank
    String searchGender;
    String namePreReformRu;
    String headerPreReformRu;
    String descriptionPreReformRu;
    String photo;

    public void createUserId(){
        this.userId = UUID.randomUUID().toString();
    }


}
