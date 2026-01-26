package com.fawry.paymentroutingengine.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillerResponse {

    private String codeBiller;

    private String name;

    private String email;
}
