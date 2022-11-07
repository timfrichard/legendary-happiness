package com.giftedconcepts.validation.core.annotations;

import com.giftedconcepts.validation.core.config.AbstractApplicationSystemIT;
import com.giftedconcepts.validation.core.model.Department;
import com.giftedconcepts.validation.core.model.PurchaseOrder;
import com.giftedconcepts.validation.core.model.PurchaseOrderLineItem;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class UnprintableCharactersTest extends AbstractApplicationSystemIT {

    @Autowired
    private ValidatorFactory validator;

    @BeforeEach
    public void initialize(){

//        factory = Validation.byDefaultProvider().configure().buildValidatorFactory();
    }

    @Test
    public void testGetCurrentStatusAdminChanged() {

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPurchaserName("Tim Richard loads of £££££ in");
        purchaseOrder.setDescription("This is a test description.");
        purchaseOrder.setDepartment(buildDepartment());
        purchaseOrder.setPurchaseOrderLineItems(buildPurchaseOrderLineItems(purchaseOrder));
        purchaseOrder.setSubmitDate(LocalDate.now());

        Set<ConstraintViolation<Object>> violations = validator.getValidator().validate(purchaseOrder);
        System.out.println("Test");

//        Assertions.assertNotNull(currentStatus);
//        Assertions.assertTrue(currentStatus.getStatus().getStatusDescriptor().equals(StatusEnum.MTL_APPROVED));
    }

    private Department buildDepartment() {

        return Department.builder().departmentId(1L).floor(21).name("Information Technology")
                .departmentLegalStatement("This is my legal statement.").build();
    }

    private List<PurchaseOrderLineItem> buildPurchaseOrderLineItems(final PurchaseOrder purchaseOrder) {

        List<PurchaseOrderLineItem> purchaseOrderLineItems = Lists.newArrayList();
        purchaseOrderLineItems.add(PurchaseOrderLineItem.builder().purchaseOrder(purchaseOrder)
                .purchaseOrderLineItemId(1L).itemDescription("Description of Item loads of £££££ in").total(BigDecimal.valueOf(100))
                .quantity(10).unitPrice(BigDecimal.valueOf(10)).build());
        purchaseOrderLineItems.add(PurchaseOrderLineItem.builder().purchaseOrder(purchaseOrder)
                .purchaseOrderLineItemId(2L)
                .itemDescription("Description with special characters loads of £££££ in 20�s")
                .total(BigDecimal.valueOf(50))
                .quantity(2).unitPrice(BigDecimal.valueOf(25)).build());
        //$$$$A trip to the store with loads of £££££ in 20�s.
        return purchaseOrderLineItems;
    }
}
