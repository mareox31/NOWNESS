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
@Alias("reportDTO")
@Data
public class ReportDTO {
    private int id;
    private int userid;
    private  LocalDateTime createddatetime;
    private  LocalDateTime editdatetime;
    private int views;
    private String contents;
    private String title;
    private int blind;
    private int deleted;
    private int boardtype;
    private int locale;
    private int subcategory;
}