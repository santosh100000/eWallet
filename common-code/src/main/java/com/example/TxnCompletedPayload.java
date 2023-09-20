package com.example;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TxnCompletedPayload {

    private Long id;

    private Boolean success;

    private String reason;

    private String requestId;
}
