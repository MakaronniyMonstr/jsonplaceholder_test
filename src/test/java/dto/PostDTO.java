package dto;

import lombok.Data;

@Data
public class PostDTO {
    private Integer userId;
    private Integer id;
    private String title;
    private String body;
}
