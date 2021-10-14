package dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PostDTO {
    private Integer id;
    private Integer userId;
    private String title;
    private String body;
}
