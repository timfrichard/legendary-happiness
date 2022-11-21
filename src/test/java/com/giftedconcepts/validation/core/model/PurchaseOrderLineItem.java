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
@UnprintableCharacters(identifierFields = {"identifiableField"},
        identifierFieldsMessage = "The identifying field value: %s")
public class PurchaseOrderLineItem {

    private String identifiableField;

    private String itemDescription;

    private Long purchaseOrderLineItemId;

    private int quantity;

    private BigDecimal total;

    private BigDecimal unitPrice;
}
