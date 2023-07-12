package highfive.nowness.service;

import highfive.nowness.domain.RankBoard;
import highfive.nowness.repository.RankBoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankBoardService {

    @Autowired
    private RankBoardRepository rankBoardRepository;

    public List<RankBoard> getRank() {
        return rankBoardRepository.getRank();
    }
}
