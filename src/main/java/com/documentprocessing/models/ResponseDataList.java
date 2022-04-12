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

    @Override
    public String toString() {
        char value='\u0022';
        return "{"+value+"NewResponse"+value+":" +response+
                "}";
    }
}
