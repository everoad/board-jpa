package com.kbj.restapi.accounts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void findByUsername() {
        // Given
        String userName = "everoad@email.com";
        String password = "1234";

        Account account = Account.builder()
                .email(userName)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        this.accountService.saveAccount(account);

        // When
        UserDetails userDetails =  accountService.loadUserByUsername(userName);

        // Then
        assertThat(this.passwordEncoder.matches(password, userDetails.getPassword())).isTrue();
    }

    @Test
    public void findByUsernameFail() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            String username = "random@email.com";
            accountService.loadUserByUsername(username);
        });
    }
}