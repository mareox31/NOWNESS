package highfive.nowness.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Alias("repliesDTO")
@Data
public class RepliesDTO {

    private int id;
    private int contentsid;
    private int userid;
    private String reply;
    private LocalDateTime createddatetime;
    private int deleted;
    private int parentid;



}


//LocalDateTime

