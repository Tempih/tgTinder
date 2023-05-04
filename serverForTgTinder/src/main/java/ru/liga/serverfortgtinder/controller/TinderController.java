package ru.liga.serverfortgtinder.controller;

import liquibase.pro.packaged.S;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.liga.serverfortgtinder.model.UserDto;
import ru.liga.serverfortgtinder.model.UserEntity;
import ru.liga.serverfortgtinder.model.UserLikeEntity;
import ru.liga.serverfortgtinder.service.TinderService;

import javax.validation.Valid;
import java.util.List;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tinder")
public class TinderController {

    private final TinderService tinderService;

    @GetMapping("{userId}/{chatId}/{currentProfileId}/next")
    public UserDto giveNextProfile(@PathVariable Integer chatId, @PathVariable String userId, @PathVariable Integer currentProfileId) {
        log.info("Получен запрос на получение следующего профиля от пользователя {} в чате {}. Id текущего пользователя {}", userId, chatId, currentProfileId);
        return tinderService.getNextProfile(chatId, userId, currentProfileId);
    }

    @GetMapping("{userId}/{chatId}/{currentProfileId}/previously")
    public UserDto givePreviouslyProfile(@PathVariable Integer chatId, @PathVariable String userId, @PathVariable Integer currentProfileId) {
        log.info("Получен запрос на получение преведущего профиля от пользователя {} в чате {}. Id текущего пользователя {}", userId, chatId, currentProfileId);
        return tinderService.getPreviouslyProfile(chatId, userId, currentProfileId);
    }

    @GetMapping("{userId}/profile")
    public UserDto giveUserInfo(@PathVariable String userId) {
        log.info("Получен запрос на получение профиля пользователя {}", userId);
        return tinderService.getUserInfo(userId);
    }

    @GetMapping("{chatId}")
    public UserDto giveUserId(@PathVariable Integer chatId) {
        log.info("Получен запрос на получение userId для чата {}", chatId);
        return tinderService.getUserId(chatId);
    }

    @GetMapping("{userId}/likes")
    public List<UserDto> givePreviouslyProfile(@PathVariable String userId) {
        log.info("Получен запрос на получение лайков для пользователя {}", userId);
        return tinderService.getLikesForUser(userId);
    }
    @PostMapping("action/like")
    public String like(@Valid @RequestBody UserLikeEntity userLikeEntity) {
        log.info("Получен запрос на постановку лайка от пользователя {} для пользователя {}", userLikeEntity.getUserId(), userLikeEntity.getLikedUserId());
        return tinderService.likeUser(userLikeEntity);
    }
    @PostMapping
    public String create(@Valid @RequestBody UserEntity userEntity) {
        log.info("Получен запрос на создание профиля от чата {}", userEntity.getChatId());
        return tinderService.addUser(userEntity);
    }

    @PutMapping("{userId}")
    public String updateUserInfo(@Valid @RequestBody UserEntity userEntity) {
        log.info("Получен запрос на изменение профиля от пользователя {}", userEntity.getUserId());
        return tinderService.updateUserInfo(userEntity);
    }

}
