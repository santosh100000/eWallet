package com.example;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletUpdatedPayload {

    private String userName;

    private String userEmail;

    private Double balance;

    private String requestId;
}
