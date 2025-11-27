package com.example.webserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TourApiResponseItemsDto {

    // item 리스트는 TourItemDto로 구성됩니다.
    @JsonProperty("item")
    private List<TourItemDto> item;
}