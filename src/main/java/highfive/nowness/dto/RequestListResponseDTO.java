package highfive.nowness.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@ToString
public class RequestListResponseDTO {
    private List<RequestDTO> requestList;
    private int currentPage;
    private int totalPages;
}
