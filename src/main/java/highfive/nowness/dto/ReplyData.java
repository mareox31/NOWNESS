package highfive.nowness.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@ToString
@Alias("replyDATA")
@Data
public class ReplyData {
    private int contentsId;
    private int userId;
    private String reply;

    //추가
    private int parentid;

}