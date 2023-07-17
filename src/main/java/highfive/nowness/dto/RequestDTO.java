package highfive.nowness.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Alias("requestDTO")
public class RequestDTO {

    private int id;
    private int userId;
    private LocalDateTime createdDatetime;
    private LocalDateTime editDatetime;
    private int views;
    private String contents;
    private String title;
    private int blind;
    private int deleted;
    private int boardType;
    private int locale;
    private int subcategory;


}


