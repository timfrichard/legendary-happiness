package com.giftedconcepts.validation.core.model;

import com.giftedconcepts.validation.core.annotations.UnprintableCharacters;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
@UnprintableCharacters
public class PurchaseOrderLineItem {

    private String itemDescription;

    private PurchaseOrder purchaseOrder;

    private Long purchaseOrderLineItemId;

    private Integer quantity;

    private BigDecimal total;

    private BigDecimal unitPrice;
}
