package com.documentprocessing.models;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class ResponseDataList {
    private List<Response> response;

}
