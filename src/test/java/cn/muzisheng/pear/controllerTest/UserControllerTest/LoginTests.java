package cn.muzisheng.pear.controllerTest.UserControllerTest;


import cn.muzisheng.pear.constant.Constant;
import cn.muzisheng.pear.dao.UserDAO;
import cn.muzisheng.pear.entity.User;
import cn.muzisheng.pear.exception.AuthorizationException;
import cn.muzisheng.pear.exception.IllegalException;
import cn.muzisheng.pear.params.LoginForm;
import cn.muzisheng.pear.properties.TokenProperties;
import cn.muzisheng.pear.properties.UserProperties;
import cn.muzisheng.pear.service.LogService;
import cn.muzisheng.pear.service.impl.UserServiceImpl;
import cn.muzisheng.pear.utils.JwtUtil;
import cn.muzisheng.pear.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class LoginTests {

    @Mock
    private UserDAO userDAO;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private TokenProperties tokenProperties;
    @Mock
    private UserProperties userProperties;
    @Mock
    private LogService logService;
    @InjectMocks
    private UserServiceImpl userService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    void login_InvalidForm() {
        LoginForm loginForm = null;
        assertThrows(IllegalException.class, () -> userService.login(request, loginForm));
    }

    @Test
    void login_InvalidEmailAndPassword() {
        LoginForm loginForm = new LoginForm();
        loginForm.setAuthToken("");
        loginForm.setEmail("");
        loginForm.setPassword("");
        assertThrows(IllegalException.class, () -> userService.login(request, loginForm), "The user passed in an illegal parameter");
    }

    @Test
    void login_ErrorPassword() {
        LoginForm loginForm = new LoginForm();
        loginForm.setAuthToken("");
        loginForm.setEmail("test@example.com");
        loginForm.setPassword("");
        assertThrows(IllegalException.class, () -> userService.login(request, loginForm), "empty password");
    }

    @Test
    void login_InvalidUser() {
        LoginForm loginForm = new LoginForm();
        loginForm.setAuthToken("");
        loginForm.setEmail("test@example.com");
        loginForm.setPassword("wrongPassword");

        when(userDAO.getUserByEmail("test@example.com")).thenReturn(null);
        ResponseEntity<Result<User>> response = userService.login(request, loginForm);

        assertEquals(Constant.USER_EXCEPTION, response.getStatusCode().value());
        assertEquals("user not exist", response.getBody().getError());
    }

    @Test
    void login_WrongDecodeHashToken_Expired() {

            LoginForm loginForm = new LoginForm();
            loginForm.setAuthToken("test");
            when(jwtUtil.getEmailFromToken("test")).thenReturn(null);

        assertThrows(AuthorizationException.class, () -> userService.login(request, loginForm), "token expired");
    }
}