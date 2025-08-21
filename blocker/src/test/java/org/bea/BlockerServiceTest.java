package org.bea;

import org.bea.dto.BlockRequest;
import org.bea.dto.BlockResponse;
import org.bea.service.BlockerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BlockerServiceTest {

    private BlockerService service;

    @BeforeEach
    void setUp() {
        service = new BlockerService();
    }

    private BlockRequest req(String login, String action) {
        BlockRequest r = new BlockRequest();
        r.setLogin(login);
        r.setAction(action);
        return r;
    }

    @Test
    void firstNineAllowed_tenthBlocked() {
        for (int i = 1; i <= 9; i++) {
            BlockResponse r = service.decide(req("john", "withdraw"));
            assertThat(r.isAllowed()).as("attempt #%s", i).isTrue();
            assertThat(r.getCount()).isEqualTo(i);
        }
        BlockResponse tenth = service.decide(req("john", "withdraw"));
        assertThat(tenth.isAllowed()).isFalse();
        assertThat(tenth.getCount()).isEqualTo(10);
    }

    @Test
    void countersArePerLoginAndAction() {
        // john|WITHDRAW → 1
        BlockResponse r1 = service.decide(req("john", "withdraw"));
        assertThat(r1.getCount()).isEqualTo(1);

        // mary|WITHDRAW → 1 (отдельный ключ)
        BlockResponse r2 = service.decide(req("mary", "withdraw"));
        assertThat(r2.getCount()).isEqualTo(1);

        // john|DEPOSIT → 1 (отдельный ключ)
        BlockResponse r3 = service.decide(req("john", "deposit"));
        assertThat(r3.getCount()).isEqualTo(1);
    }

    @Test
    void nullsAreNormalizedToAnonymousAndUNKNOWN_uppercasedAction() {
        BlockRequest r = new BlockRequest(); // login null, action null
        BlockResponse a1 = service.decide(r);
        assertThat(a1.getCount()).isEqualTo(1);
        // второй вызов с теми же null → тот же ключ "anonymous|UNKNOWN"
        BlockResponse a2 = service.decide(new BlockRequest());
        assertThat(a2.getCount()).isEqualTo(2);
    }
}
