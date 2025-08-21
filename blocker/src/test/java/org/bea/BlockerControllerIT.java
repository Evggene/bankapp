package org.bea;

import org.bea.dto.BlockRequest;
import org.bea.dto.BlockResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BlockerControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String url() {
        return "http://localhost:" + port + "/check";
    }

    private BlockRequest req(String login, String action) {
        BlockRequest r = new BlockRequest();
        r.setLogin(login);
        r.setAction(action);
        return r;
    }

    @Test
    void tenthRequestIsBlocked() {
        for (int i = 1; i <= 9; i++) {
            BlockResponse r = rest.postForObject(url(), req("john","withdraw"), BlockResponse.class);
            assertThat(r.isAllowed()).as("attempt #%s", i).isTrue();
            assertThat(r.getCount()).isEqualTo(i);
        }
        BlockResponse tenth = rest.postForObject(url(), req("john","withdraw"), BlockResponse.class);
        assertThat(tenth.isAllowed()).isFalse();
        assertThat(tenth.getCount()).isEqualTo(10);
    }
}
