package example.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TxnStatusDTO {

    private String status;

    private String reason;
}
