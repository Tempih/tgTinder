package ru.liga.serverfortgtinder.model;

import lombok.*;
import ru.liga.serverfortgtinder.service.PhotoService;
import ru.liga.serverfortgtinder.service.WordService;

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
    Long chatId;
    @NotBlank
    String gender;
    @NotBlank
    String name;
    String header;
    String description;
    @NotBlank
    String searchGender;
    PhotoService photoService;
    WordService wordService;
    String nameOldSlavonic;
    String headerOldSlavonic;
    String descriptionOldSlavonic;
    String photo;

    public void createUserId(){
        this.userId = UUID.randomUUID().toString();
    }
    public void createOldSlavonicNameHeaderDescription(){
        this.nameOldSlavonic = wordService.convertToSlovenian(this.name);
        this.headerOldSlavonic = wordService.convertToSlovenian(this.header);
        this.descriptionOldSlavonic = wordService.convertToSlovenian(this.description);
    }
    public void createPhoto(){
            this.photo = photoService.signImageAdaptBasedOnImage(this.headerOldSlavonic, this.descriptionOldSlavonic);
    }

}
