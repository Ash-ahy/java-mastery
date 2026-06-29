package com.mastery.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户创建/更新请求
 */
@Data
public class UserDTO {
    
    @NotNull(message = "ID不能为空", groups = Update.class)
    @Null(message = "创建时不能指定ID", groups = Create.class)
    private Long id;
    
    @NotBlank(message = "用户名不能为空", groups = {Create.class, Update.class})
    @Size(min = 3, max = 20, message = "用户名长度3-20字符")
    private String username;
    
    @NotBlank(message = "密码不能为空", groups = Create.class)
    @Size(min = 6, max = 30, message = "密码长度6-30字符")
    private String password;
    
    private String nickname;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    private Integer age;
    
    private Integer gender;
    
    private Integer status;
    
    private java.util.List<Long> roleIds;
    
    // 分组接口
    public interface Create {}
    public interface Update {}
}
