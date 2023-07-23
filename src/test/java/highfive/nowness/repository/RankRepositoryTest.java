package highfive.nowness.repository;

import highfive.nowness.dto.RankBoardDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class RankRepositoryTest {

    @Autowired
    private RankBoardRepository rankBoardRepository;

    @Test
    void getTest() {
        //List<highfive.nowness.domain.RankBoard> test = rankBoardRepository.getRank();
        //System.out.println(test.get(0).getId());
        //System.out.println(test.get(0).getDate());
        //System.out.println(test.get(0).getNickname());
        //System.out.println(test.get(0).getContents());

        Map<String, Object> params = new HashMap<>();

        params.put("ST", "year");
        params.put("SM", "likeCount");

        System.out.println((String)params.get("ST"));
        System.out.println((String)params.get("SM"));

        List<RankBoardDTO> rankBoards = rankBoardRepository.getRank(params);

        //System.out.println(rankBoards.get(0).getTitle());
        //System.out.println(rankBoards.get(1).getTitle());
        //System.out.println(rankBoards.get(2).getTitle());
    }
}