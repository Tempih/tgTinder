package ru.liga.serverfortgtinder.controller;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.liga.serverfortgtinder.model.UserDto;
import ru.liga.serverfortgtinder.model.UserEntity;
import ru.liga.serverfortgtinder.model.UserLikeEntity;
import ru.liga.serverfortgtinder.service.TinderService;

import java.util.List;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/tinder")
public class TinderController {

    @Autowired
    TinderService tinderService;

    @GetMapping("{userid}/{chatId}/{currentProfileId}/next")
    public UserDto giveNextProfile(@PathVariable Long chatId, @PathVariable String userid, @PathVariable Long currentProfileId) {
        log.info("Получен запрос на получение следующего профиля от пользователя {} в чате {}. Id текущего пользователя {}", userid, chatId, currentProfileId);
        return tinderService.getNextProfile(chatId, userid, currentProfileId);
    }

    @GetMapping("{userid}/{chatId}/{currentProfileId}/previously")
    public UserDto givePreviouslyProfile(@PathVariable Long chatId, @PathVariable String userid, @PathVariable Long currentProfileId) {
        log.info("Получен запрос на получение преведущего профиля от пользователя {} в чате {}. Id текущего пользователя {}",userid, chatId, currentProfileId);
        return tinderService.getPreviouslyProfile(chatId, userid, currentProfileId);
    }

    @GetMapping("{userid}/profile")
    public UserDto giveUserInfo(@PathVariable String userid) {
        log.info("Получен запрос на получение профиля пользователя {}", userid);
        return tinderService.getUserInfo(userid);
    }

    @GetMapping("/{chatId}")
    public UserDto giveUserId(@PathVariable Long chatId) {
        log.info("Получен запрос на получение userId для чата {}", chatId);
        return tinderService.getUserId(chatId);
    }

    @GetMapping("/{userid}/likes")
    public List<UserDto> givePreviouslyProfile(@PathVariable String userid) {
        log.info("Получен запрос на получение лайков для пользователя {}", userid);
        return tinderService.getLikesForUser(userid);
    }
    @PostMapping("/action/like/")
    public int like(@Valid @RequestBody UserLikeEntity userLikeEntity) {
        log.info("Получен запрос на постановку лайка от пользователя {} для пользователя {}", userLikeEntity.getUserId(), userLikeEntity.getLikedUserId());
        return tinderService.likeUser(userLikeEntity);
    }
    @PostMapping
    public int create(@Valid @RequestBody UserEntity userEntity) {
        log.info("Получен запрос на создание профиля от чата {}", userEntity.getChatId());
        return tinderService.addUser(userEntity);
    }

    @PutMapping("/{userid}/")
    public int updateUserInfo(@RequestBody UserEntity userEntity) {
        log.info("Получен запрос на изменение профиля от пользователя {}", userEntity.getUserId());
        return tinderService.updateUserInfo(userEntity);
    }

}
