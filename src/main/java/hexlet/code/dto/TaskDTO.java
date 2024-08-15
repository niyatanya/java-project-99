package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class TaskDTO {
    private long id;
    private int index;
    private Timestamp createdAt;
    private long assigneeId;
    private String title;
    private String content;
    private String status;
}
