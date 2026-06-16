package com.talentbridge.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Menu {
    
    private Long id;
    
    private String menuName;
    
    private String menuPath;
    
    private String menuIcon;
    
    private Long parentId;
    
    private Integer sortOrder;
    
    private LocalDateTime createTime;
}
