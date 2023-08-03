package highfive.nowness.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

@Data
@Builder
@Alias("likesDTO")
public class LikesDTO {

    private int id;
    private int contents_id;
    private int user_id;
    private LocalDateTime like_datetime;


}

//import java.sql.Timestamp;
