package highfive.nowness.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@ToString
@Alias("reportsDTO")
@Data
public class ReportsDTO {
    private int id;
    private int reportedContentsId;
    private int reportUserId; // 신고한 사용자의 ID
    private String reportReason; // 신고 사유
}
