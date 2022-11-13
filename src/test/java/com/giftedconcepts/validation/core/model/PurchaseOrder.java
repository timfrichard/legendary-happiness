package com.giftedconcepts.validation.core.model;

import com.giftedconcepts.validation.core.annotations.UnprintableCharacters;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
@UnprintableCharacters
@ToString
public class PurchaseOrder {

    @Valid
    private Department department;

    private String description;

    private Long purchaseOrderId;

    private String purchaserName;

    private List<@Valid PurchaseOrderLineItem> purchaseOrderLineItems;

    @NotNull(message = "Submit date is required for a purchase order.")
    private LocalDate submitDate;
}
