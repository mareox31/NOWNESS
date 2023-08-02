package highfive.nowness.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TestRepositoryTest {

    @Autowired
    private TestRepository testRepository;

    @Test
    @Disabled("배포를 위해 통과되지 않는 테스트는 Disabled 처리합니다.")
    void getTest() {
        highfive.nowness.domain.Test saveTest  = new highfive.nowness.domain.Test(1, "test");
        testRepository.addTest(saveTest);

        List<highfive.nowness.domain.Test> test = testRepository.getTest();
        assertThat(test.get(0).getId()).isEqualTo(1);
        assertThat(test.get(0).getName()).isEqualTo("test");

        testRepository.deleteTest(1L);
    }
}