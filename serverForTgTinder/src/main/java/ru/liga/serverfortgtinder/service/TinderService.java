package ru.liga.serverfortgtinder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.liga.serverfortgtinder.model.UserDto;
import ru.liga.serverfortgtinder.model.UserEntity;
import ru.liga.serverfortgtinder.model.UserLikeEntity;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class TinderService {
    public static final int  SUCCESS_STATUS = 1;
    private final WordService wordService;
    private final PhotoService photoService;
    private final SpringJdbcConnectionProvider springJdbcConnectionProvider;
    private final String RETURN_SUCCESS_STATUS = "SUCCESS";
    private final String RETURN_FAIL_STATUS = "FAIL";


    public String addUser(UserEntity userEntity){
        log.debug("Создаем профиль пользователя");
        userEntity.createUserId();
        userEntity.setNamePreReformRu(wordService.convertToSlavonic(userEntity.getName()));
        userEntity.setHeaderPreReformRu(wordService.convertToSlavonic(userEntity.getHeader()));
        userEntity.setDescriptionPreReformRu(wordService.convertToSlavonic(userEntity.getDescription()));
        userEntity.setPhoto(photoService.signImageAdaptBasedOnImage(userEntity.getHeaderPreReformRu(), userEntity.getDescriptionPreReformRu()));
        userEntity.setSearchGender(userEntity.getSearchGender());
        log.debug("Профиль для пользователя {} создан", userEntity.getUserId());
        log.debug("Записываем в БД оригинальный профиль пользователя {}", userEntity.getUserId());
        int resultInputToUserTable = springJdbcConnectionProvider.addOriginalUserInfo(userEntity);
        log.debug("Записываем в БД профиль на старославянском с фото пользователя {}", userEntity.getUserId());
        int resultInputToUserViewTable = springJdbcConnectionProvider.addModifiedUserInfo(userEntity);
        if (resultInputToUserTable == SUCCESS_STATUS && resultInputToUserViewTable == SUCCESS_STATUS){
            return RETURN_SUCCESS_STATUS;
        }
        return RETURN_FAIL_STATUS;
    }

    public UserDto getNextProfile(Integer chatId, String userId, Integer currentProfileId){
        log.debug("Получаем из БД следующий профиль для пользователя {} в чате {}. Текущий профиль {}", userId, chatId, currentProfileId);
        return springJdbcConnectionProvider.getNextProfile(chatId, userId, currentProfileId);
    }
    public UserDto getPreviouslyProfile(Integer chatId, String userId, Integer currentProfileId){
        log.debug("Получаем из БД преведущий профиль для пользователя {} в чате {}. Текущий профиль {}", userId, chatId, currentProfileId);
        return springJdbcConnectionProvider.getPreviouslyProfile(chatId, userId, currentProfileId);
    }

    public String likeUser(UserLikeEntity userLikeEntity){
        log.debug("Пользователь {} поставил лайк пользователю {}", userLikeEntity.getUserId(), userLikeEntity.getLikedUserId());
        int resultInputToUserLikeTable = springJdbcConnectionProvider.addUserLike(userLikeEntity);
        if (resultInputToUserLikeTable == SUCCESS_STATUS){
            return RETURN_SUCCESS_STATUS;
        }
        return RETURN_FAIL_STATUS;
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

    public UserDto getUserId(Integer chatId){
        log.debug("Получаем из БД userId для чата {}", chatId);
        return springJdbcConnectionProvider.getUserId(chatId);
    }

    public UserDto getUserInfo(String userId){
        log.debug("Получаем из БД профиль для пользователя {}", userId);
        return springJdbcConnectionProvider.getUserInfo(userId);
    }

    public String updateUserInfo(UserEntity userEntity){
        log.debug("Переводим на старославянский и создаем новое фото для пользователя {}", userEntity.getUserId());
        userEntity.setNamePreReformRu(wordService.convertToSlavonic(userEntity.getName()));
        userEntity.setHeaderPreReformRu(wordService.convertToSlavonic(userEntity.getHeader()));
        userEntity.setDescriptionPreReformRu(wordService.convertToSlavonic(userEntity.getDescription()));
        userEntity.setPhoto(photoService.signImageAdaptBasedOnImage(userEntity.getHeaderPreReformRu(), userEntity.getDescriptionPreReformRu()));
        userEntity.setSearchGender(userEntity.getSearchGender());
        log.debug("Записываем в БД измененный профиль пользователя {}", userEntity.getUserId());
        int resultInputToUserTable = springJdbcConnectionProvider.updateOriginalUserInfo(userEntity);
        log.debug("Записываем в БД измененный профиль на старославянском с фото пользователя {}", userEntity.getUserId());
        int resultInputToUserViewTable = springJdbcConnectionProvider.updateModifiedUserInfo(userEntity);
        if (resultInputToUserTable == SUCCESS_STATUS && resultInputToUserViewTable == SUCCESS_STATUS){
            return RETURN_SUCCESS_STATUS;
        }
        return RETURN_FAIL_STATUS;
    }

}
