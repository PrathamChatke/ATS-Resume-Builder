package com.atsresume.jobposting;

import jakarta.validation.constraints.NotBlank;

public record CreateJobPostingRequest(@NotBlank String title, @NotBlank String description) {
}
