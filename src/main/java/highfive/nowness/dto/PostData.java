package highfive.nowness.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@ToString
@Alias("postDATA")
@Data
public class PostData {
    private int userId;
    private String contents;
    private String title;
    private int boardType;
    private int locale;
    private int subcategory;

    private int id;//글번호 ,글수정때문에 추가.
    
}

//글쓰기 저장용.