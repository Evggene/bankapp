package org.bea.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockResponse {
    private boolean allowed;
    private String reason;
    private int count;
}
