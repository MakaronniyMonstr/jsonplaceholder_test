package pojo;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class PojoModel {
    private Long userId;
    private Long id;
    private String title;
    private String body;
}
