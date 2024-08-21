package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class LabelDTO {
    private Long id;
    private String name;
    private Timestamp createdAt;
}
