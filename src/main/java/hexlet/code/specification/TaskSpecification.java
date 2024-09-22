package hexlet.code.specification;

import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TaskSpecification {

    private final LabelRepository labelRepository;

    public Specification<Task> build(TaskParamsDTO params) {
        return withAssigneeId(params.getAssigneeId())
                .and(withLabelId(params.getLabelId()))
                .and(withTitleCont(params.getTitleCont()))
                .and(withTaskStatus(params.getStatus()));
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, criteriaBuilder) -> assigneeId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("assignee").get("id"),
                assigneeId);
    }

    private Specification<Task> withLabelId(Long labelId) {
        return (root, query, criteriaBuilder) -> labelId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.isMember(labelRepository.findById(labelId).orElseThrow(),
                root.get("labels"));
    }

    private Specification<Task> withTaskStatus(String status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("taskStatus").get("slug"), status);

    }

    private Specification<Task> withTitleCont(String titleCont) {
        return (root, query, criteriaBuilder) -> titleCont == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + titleCont + "%");
    }
}
