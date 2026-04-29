package com.babydiary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BabyDTO {
    @NotBlank(message = "宝宝名字不能为空")
    private String name;
    private Integer gender;
    @NotNull(message = "生日不能为空")
    private LocalDate birthday;
}
