package com.smartteaching.service.auth;

import com.smartteaching.common.dto.user.UserLoginDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_shouldRejectRoleMismatch() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        dto.setRole("student");

        User user = new User();
        user.setUsername("admin");
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        user.setRole("admin");
        user.setStatus(1);

        when(userMapper.selectOne(any())).thenReturn(user);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("角色");
    }
}
