package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {
    private long id;
    private int index;
    private Timestamp createdAt;
    private String title;
    private String content;
    private String status;
    private Set<Long> taskLabelIds;

    @JsonProperty("assignee_id")
    private long assigneeId;
}
