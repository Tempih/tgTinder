package ru.liga.serverfortgtinder.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.liga.serverfortgtinder.model.UserDto;
import ru.liga.serverfortgtinder.model.UserEntity;
import ru.liga.serverfortgtinder.model.UserLikeEntity;

import java.util.List;
@Slf4j
@Service
public class TinderService {

    @Autowired
    private WordService wordService;
    private PhotoService photoService;


    private final SpringJdbcConnectionProvider springJdbcConnectionProvider;

    public TinderService(PhotoService photoService, SpringJdbcConnectionProvider springJdbcConnectionProvider) {
        this.photoService = photoService;
        this.springJdbcConnectionProvider = springJdbcConnectionProvider;
    }


    @SneakyThrows
    public int addUser(UserEntity userEntity){
        log.debug("Создаем профиль пользователя");
        userEntity.createUserId();
        userEntity.setWordService(wordService);
        userEntity.setPhotoService(photoService);
        userEntity.createOldSlavonicNameHeaderDescription();
        userEntity.createPhoto();
        userEntity.setSearchGender(userEntity.getSearchGender());
        log.debug("Профиль для пользователя {} создан", userEntity.getUserId());
        log.debug("Записываем в БД оригинальный профиль пользователя {}", userEntity.getUserId());
        springJdbcConnectionProvider.addOriginalUserInfo(userEntity);
        log.debug("Записываем в БД профиль на старославянском с фото пользователя {}", userEntity.getUserId());
        return springJdbcConnectionProvider.addModifiedUserInfo(userEntity);
    }

    public UserDto getNextProfile(Long chatId, String userId, Long currentProfileId){
        log.debug("Получаем из БД следующий профиль для пользователя {} в чате {}. Текущий профиль {}", userId, chatId, currentProfileId);
        return springJdbcConnectionProvider.getNextProfile(chatId, userId, currentProfileId);
    }
    public UserDto getPreviouslyProfile(Long chatId, String userId, Long currentProfileId){
        log.debug("Получаем из БД преведущий профиль для пользователя {} в чате {}. Текущий профиль {}", userId, chatId, currentProfileId);
        return springJdbcConnectionProvider.getPreviouslyProfile(chatId, userId, currentProfileId);
    }

    public int likeUser(UserLikeEntity userLikeEntity){
        log.debug("Пользователь {} поставил лайк пользователю {}", userLikeEntity.getUserId(), userLikeEntity.getLikedUserId());
        return springJdbcConnectionProvider.addUserLike(userLikeEntity);
    }
    public List<UserDto> getLikesForUser(String userId){
        log.debug("Получаем из БД взаимные лайки для пользователя {}", userId);
        List<UserDto> userLikesList = springJdbcConnectionProvider.getMutualLikeUsers(userId);
        List<String> userAddedInListLikes = userLikesList.stream()
                .map(UserDto::getUserIdProfile)
                .toList();
        log.debug("Получаем из БД лайки пользователя {}", userId);
        userLikesList.addAll(springJdbcConnectionProvider.getUserLikes(userId, userAddedInListLikes));
        log.debug("Получаем из БД лайки поставленные пользователю {}", userId);
        userLikesList.addAll(springJdbcConnectionProvider.getLikesForUser(userId, userAddedInListLikes));
        return userLikesList;
    }

    public UserDto getUserId(Long chatId){
        log.debug("Получаем из БД userId для чата {}", chatId);
        return springJdbcConnectionProvider.getUserId(chatId);
    }

    public UserDto getUserInfo(String userId){
        log.debug("Получаем из БД профиль для пользователя {}", userId);
        return springJdbcConnectionProvider.getUserInfo(userId);
    }

    public int updateUserInfo(UserEntity userEntity){
        log.debug("Переводим на старославянский и создаем новое фото для пользователя {}", userEntity.getUserId());
        userEntity.setWordService(wordService);
        userEntity.setPhotoService(photoService);
        userEntity.createOldSlavonicNameHeaderDescription();
        userEntity.createPhoto();
        userEntity.setSearchGender(userEntity.getSearchGender());
        log.debug("Записываем в БД измененный профиль пользователя {}", userEntity.getUserId());
        springJdbcConnectionProvider.updateOriginalUserInfo(userEntity);
        log.debug("Записываем в БД измененный профиль на старославянском с фото пользователя {}", userEntity.getUserId());
        return springJdbcConnectionProvider.updateModifiedUserInfo(userEntity);
    }

}
