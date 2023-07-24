package highfive.nowness.repository;

import highfive.nowness.dto.RankBoardDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface RankBoardRepository {
    List<RankBoardDTO> getRank(Map<String, Object> params);
}
