package ru.liga.serverfortgtinder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.liga.serverfortgtinder.model.UserDto;
import ru.liga.serverfortgtinder.model.UserEntity;
import ru.liga.serverfortgtinder.model.UserLikeEntity;
import ru.liga.serverfortgtinder.service.PhotoService;
import ru.liga.serverfortgtinder.service.SpringJdbcConnectionProvider;
import ru.liga.serverfortgtinder.service.TinderService;
import ru.liga.serverfortgtinder.service.WordService;

import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class TinderServiceTest {

    private final String SUCCESS = "SUCCESS";
    private final String FAIL = "FAIL";
    @Mock
    private SpringJdbcConnectionProvider springJdbcConnectionProvider;
    @Mock
    private WordService wordService;
    @Mock
    private PhotoService photoService;
    @InjectMocks
    private TinderService tinderService;

    @Test
    public void testGetNextProfile() {
        UserDto userDto = new UserDto();
        when(springJdbcConnectionProvider.getNextProfile(anyInt(), anyString(), anyInt())).thenReturn(userDto);

        UserDto result = tinderService.getNextProfile(1, "1", 2);

        assertEquals(userDto, result);
        verify(springJdbcConnectionProvider).getNextProfile(1, "1", 2);
    }

    @Test
    public void testAddUserIfDataWriteToDb() {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("Name");
        userEntity.setHeader("Header");
        userEntity.setDescription("Description");
        userEntity.setSearchGender("SearchGender");

        when(wordService.convertToSlavonic(anyString())).thenReturn("Slavonic");
        when(photoService.signImageAdaptBasedOnImage(anyString(), anyString())).thenReturn("SignedImage");
        when(springJdbcConnectionProvider.addOriginalUserInfo(any(UserEntity.class))).thenReturn(1);
        when(springJdbcConnectionProvider.addModifiedUserInfo(any(UserEntity.class))).thenReturn(1);

        String result = tinderService.addUser(userEntity);
        assertEquals(SUCCESS, result);

        verify(wordService).convertToSlavonic("Name");
        verify(wordService).convertToSlavonic("Header");
        verify(wordService).convertToSlavonic("Description");
        verify(photoService).signImageAdaptBasedOnImage("Slavonic", "Slavonic");
        verify(springJdbcConnectionProvider).addOriginalUserInfo(userEntity);
        verify(springJdbcConnectionProvider).addModifiedUserInfo(userEntity);
    }

    @Test
    public void testAddUserIfDataNotWriteToDb() {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("Name");
        userEntity.setHeader("Header");
        userEntity.setDescription("Description");
        userEntity.setSearchGender("SearchGender");

        when(wordService.convertToSlavonic(anyString())).thenReturn("Slavonic");
        when(photoService.signImageAdaptBasedOnImage(anyString(), anyString())).thenReturn("SignedImage");
        when(springJdbcConnectionProvider.addOriginalUserInfo(any(UserEntity.class))).thenReturn(0);
        when(springJdbcConnectionProvider.addModifiedUserInfo(any(UserEntity.class))).thenReturn(1);

        String result = tinderService.addUser(userEntity);
        assertEquals(FAIL, result);

        verify(wordService).convertToSlavonic("Name");
        verify(wordService).convertToSlavonic("Header");
        verify(wordService).convertToSlavonic("Description");
        verify(photoService).signImageAdaptBasedOnImage("Slavonic", "Slavonic");
        verify(springJdbcConnectionProvider).addOriginalUserInfo(userEntity);
        verify(springJdbcConnectionProvider).addModifiedUserInfo(userEntity);
    }

    @Test
    public void testGetPreviouslyProfile() {
        UserDto userDto = new UserDto();
        when(springJdbcConnectionProvider.getPreviouslyProfile(anyInt(), anyString(), anyInt())).thenReturn(userDto);

        UserDto result = tinderService.getPreviouslyProfile(1, "1", 4);

        assertEquals(userDto, result);
        verify(springJdbcConnectionProvider).getPreviouslyProfile(1, "1", 4);
    }

    @Test
    public void testLikeUser() {
        UserLikeEntity userLikeEntity = new UserLikeEntity();
        when(springJdbcConnectionProvider.addUserLike(any(UserLikeEntity.class))).thenReturn(1);

        String result = tinderService.likeUser(userLikeEntity);

        assertEquals(SUCCESS, result);
        verify(springJdbcConnectionProvider).addUserLike(userLikeEntity);
    }

    @Test
    public void testGetLikesForUser() {
        UserDto user1 = new UserDto();
        UserDto user2 = new UserDto();

        List<UserDto> mutualLikes = new ArrayList<>();
        mutualLikes.add(user1);
        mutualLikes.add(user2);

        List<UserDto> userLikes = new ArrayList<>();
        userLikes.add(user1);

        List<UserDto> likesForUser = new ArrayList<>();
        likesForUser.add(user2);

        // Настраиваем мок для метода getMutualLikeUsers
        when(springJdbcConnectionProvider.getMutualLikeUsers("1")).thenReturn(mutualLikes);

        // Настраиваем мок для методов getUserLikes и getLikesForUser
        when(springJdbcConnectionProvider.getUserLikes(eq("1"), anyList())).thenReturn(userLikes);
        when(springJdbcConnectionProvider.getLikesForUser(eq("1"), anyList())).thenReturn(likesForUser);

        // Вызываем тестируемый метод
        List<UserDto> result = tinderService.getLikesForUser("1");
        // Проверяем результат
        assertNotNull(result);
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        verify(springJdbcConnectionProvider).getMutualLikeUsers("1");
    }

    @Test
    public void testGetUserId() {
        UserDto userDto = new UserDto();
        when(springJdbcConnectionProvider.getUserId(anyInt())).thenReturn(userDto);

        UserDto result = tinderService.getUserId(1);

        assertEquals(userDto, result);
        verify(springJdbcConnectionProvider).getUserId(1);
    }

    @Test
    public void testGetUserInfo() {
        UserDto userDto = new UserDto();
        when(springJdbcConnectionProvider.getUserInfo(anyString())).thenReturn(userDto);

        UserDto result = tinderService.getUserInfo("1");

        assertEquals(userDto, result);
        verify(springJdbcConnectionProvider).getUserInfo("1");
    }

    @Test
    public void testUpdateUserInfo() {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("Name");
        userEntity.setHeader("Header");
        userEntity.setDescription("Description");
        userEntity.setSearchGender("SearchGender");

        when(wordService.convertToSlavonic(anyString())).thenReturn("Slavonic");
        when(photoService.signImageAdaptBasedOnImage(anyString(), anyString())).thenReturn("SignedImage");
        when(springJdbcConnectionProvider.updateOriginalUserInfo(any(UserEntity.class))).thenReturn(1);
        when(springJdbcConnectionProvider.updateModifiedUserInfo(any(UserEntity.class))).thenReturn(1);

        String result = tinderService.updateUserInfo(userEntity);
        assertEquals(SUCCESS, result);

        verify(wordService).convertToSlavonic("Name");
        verify(wordService).convertToSlavonic("Header");
        verify(wordService).convertToSlavonic("Description");
        verify(photoService).signImageAdaptBasedOnImage("Slavonic", "Slavonic");
        verify(springJdbcConnectionProvider).updateOriginalUserInfo(userEntity);
        verify(springJdbcConnectionProvider).updateModifiedUserInfo(userEntity);
    }
}
