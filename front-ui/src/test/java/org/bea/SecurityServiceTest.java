package org.bea;

import org.bea.SecurityService;
import org.bea.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

class SecurityServiceTest {

    @Test
    void loadUserByUsername_mapsRemoteUserToSpringUser() {
        RestTemplate rt = mock(RestTemplate.class);
        SecurityService service = new SecurityService(rt);

        User u = new User();
        u.setUsername("john");
        u.setPassword("{noop}123");
        u.setEnabled(true);

        when(rt.getForObject(startsWith("http://gateway/accounts/loadUser?user="),
                eq(User.class))).thenReturn(u);

        UserDetails ud = service.loadUserByUsername("john");
        assertThat(ud.getUsername()).isEqualTo("john");
        assertThat(ud.getPassword()).isEqualTo("{noop}123");
        assertThat(ud.isEnabled()).isTrue();
        assertThat(ud.getAuthorities()).extracting("authority").contains("ROLE_USER");
    }
}
