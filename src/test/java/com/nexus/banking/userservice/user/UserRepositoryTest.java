package com.nexus.banking.userservice.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("should save and retrieve user by email")
    void shouldSaveAndFindUserByEmail() {
        // given
        User user = User.builder()
                        .email("test@nexus.com")
                        .firstName("Test")
                        .lastName("User")
                        .locked(false)
                        .build();

        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByEmail("test@nexus.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get()
                        .getEmail()).isEqualTo("test@nexus.com");
    }
}
