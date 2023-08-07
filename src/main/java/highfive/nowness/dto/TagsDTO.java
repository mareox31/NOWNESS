package highfive.nowness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Alias("tagsDTO")
@Data
public class TagsDTO {

    private int id;
    private int contentsid;
    private String tag;



}


