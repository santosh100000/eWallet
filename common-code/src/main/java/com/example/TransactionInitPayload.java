package com.example;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionInitPayload {

    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private Double amount;

    private String remark;

    private String requestId;
}
