package com.babydiary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiDoctorDTO {
    @NotBlank(message = "问题不能为空")
    private String question;
    private boolean anonymous;
}
