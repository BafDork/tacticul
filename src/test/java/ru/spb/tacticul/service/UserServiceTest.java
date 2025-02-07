package ru.spb.tacticul.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.spb.tacticul.dto.UserDTO;
import ru.spb.tacticul.exception.ResourceNotFoundException;
import ru.spb.tacticul.mapper.UserMapper;
import ru.spb.tacticul.model.User;
import ru.spb.tacticul.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Validator validator;

    @InjectMocks
    private UserService userService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void testGetAllUsers() {
        User user = new User(1L, "testUser", "test@example.com", "password");
        UserDTO userDTO = new UserDTO(1L, "testUser", "test@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.userToUserDTO(user)).thenReturn(userDTO);

        List<UserDTO> users = userService.getAll();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(userDTO, users.get(0));
    }

    @Test
    void testGetById_UserExists() {
        User user = new User(1L, "testUser", "test@example.com", "password");
        UserDTO userDTO = new UserDTO(1L, "testUser", "test@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.userToUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(userDTO, result);
    }

    @Test
    void testGetById_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getById(1L));
    }

    @Test
    void testCreateUser_Valid() {
        UserDTO userDTO = new UserDTO(null, "testUser", "test@example.com");
        User user = new User(null, "testUser", "test@example.com", "password");
        User savedUser = new User(1L, "testUser", "test@example.com", "password");
        UserDTO savedUserDTO = new UserDTO(1L, "testUser", "test@example.com");

        when(userMapper.userDTOToUser(userDTO)).thenReturn(user);
        when(validator.validate(user)).thenReturn(Set.of());
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.userToUserDTO(savedUser)).thenReturn(savedUserDTO);

        UserDTO result = userService.create(userDTO);

        assertNotNull(result);
        assertEquals(savedUserDTO, result);
    }

    @Test
    void testCreateUser_Invalid() {
        UserDTO userDTO = new UserDTO(null, "", "invalid");
        User user = new User(null, "", "invalid", "password");

        when(userMapper.userDTOToUser(userDTO)).thenReturn(user);
        when(validator.validate(user)).thenReturn(Set.of(mock(ConstraintViolation.class)));

        assertThrows(ConstraintViolationException.class, () -> userService.create(userDTO));
    }

    @Test
    void testUpdateUser_UserExists_Valid() {
        User existingUser = new User(1L, "testUser", "test@example.com", "password");
        UserDTO userDTO = new UserDTO(1L, "updatedUser", "updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(validator.validate(any(User.class))).thenReturn(Set.of());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.userToUserDTO(any(User.class))).thenReturn(userDTO);

        UserDTO result = userService.update(1L, userDTO);

        assertNotNull(result);
        assertEquals(userDTO.login(), result.login());
        assertEquals(userDTO.email(), result.email());
    }

    @Test
    void testUpdateUser_UserExists_Invalid() {
        User existingUser = new User(1L, "testUser", "test@example.com", "password");
        UserDTO userDTO = new UserDTO(1L, "", "invalid");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(validator.validate(any(User.class))).thenReturn(Set.of(mock(ConstraintViolation.class)));

        assertThrows(ConstraintViolationException.class, () -> userService.update(1L, userDTO));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserDTO userDTO = new UserDTO(1L, "updatedUser", "updated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.update(1L, userDTO));
    }

    @Test
    void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        userService.delete(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> userService.delete(1L));
    }
}
