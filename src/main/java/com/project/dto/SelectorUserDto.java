package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SelectorUserDto {
    private UUID id;
    private String name;
}
