package highfive.nowness.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RankRepositoryTest {

    @Autowired
    private RankBoardRepository rankBoardRepository;

    @Test
    void getTest() {
        List<highfive.nowness.domain.RankBoard> test = rankBoardRepository.getRank();
        System.out.println(test.get(0).getId());
        System.out.println(test.get(0).getDate());
        System.out.println(test.get(0).getNickname());
        System.out.println(test.get(0).getContents());

    }
}