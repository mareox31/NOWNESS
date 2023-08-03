package highfive.nowness.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@ToString
@Alias("fileDATA")
@Data
public class FileData {
    private long id;
    private long contentsid; // Change this property name to 'contentsId'
    private String orginName; // The original name of the file
    private String saveName;  // The name including UUID, saved in the server
    private String path;      // The path where the file is stored
    private long size;        // The file size
    private String ext;       // The file extension



    // Add the getter method for 'contentsId'
    public long getContentsId() {
        return contentsid;
    }


    public String getOriginalFilename() {
        return orginName;
    }
}
