package highfive.nowness.repository;

import highfive.nowness.domain.RankBoard;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface RankBoardRepository {
    List<RankBoard> getRank();
}
