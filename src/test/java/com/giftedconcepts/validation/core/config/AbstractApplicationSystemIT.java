package com.giftedconcepts.validation.core.config;

import com.giftedconcepts.validation.core.model.Department;
import com.giftedconcepts.validation.core.model.PurchaseOrder;
import com.giftedconcepts.validation.core.model.PurchaseOrderLineItem;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractApplicationSystemIT {

    protected Random random;
    @LocalServerPort
    private int port;

    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    protected Department buildDepartment(final String name, String departmentLegalStatement) {

        return Department.builder().departmentId(random.nextLong()).floor(random.nextInt()).name(name)
                .departmentLegalStatement(departmentLegalStatement).build();
    }

    protected PurchaseOrder buildPurchaseOrder(final String purchaserName, final String description,
                                               final Department department,
                                               final List<PurchaseOrderLineItem> purchaseOrderLineItems) {

        return PurchaseOrder.builder().purchaserName(purchaserName)
                .description(description).department(department).purchaseOrderLineItems(purchaseOrderLineItems)
                .submitDate(LocalDate.now()).build();
    }

    protected List<PurchaseOrderLineItem> buildPurchaseOrderLineItems(final boolean initialize,
                                                                      final int numberOfAdditionalLineItems,
                                                                      final List<PurchaseOrderLineItem> purchaseOrderLineItems,
                                                                      final PurchaseOrderLineItem purchaseOrderLineItem) {

        if (initialize) {
            List<PurchaseOrderLineItem> purchaseOrderLineItems1 = Lists.newArrayList();

            if (numberOfAdditionalLineItems > 0) {
                for (int i = 0; i < numberOfAdditionalLineItems; ++i) {
                    /* Number between 0 and 50 */
                    int quantity = random.nextInt(51);
                    float unitPrice = getUnitPrice();

                    purchaseOrderLineItems1.add(buildPurchaseOrderLineItem(quantity, unitPrice,
                            RandomStringUtils.randomAlphanumeric(20), "This is my identity."));
                }
            }

            return purchaseOrderLineItems1;
        }

        purchaseOrderLineItems.add(purchaseOrderLineItem);
        return purchaseOrderLineItems;
    }

    protected PurchaseOrderLineItem buildPurchaseOrderLineItem(final int quantity, final float unitPrice,
                                                               final String description, final String identifiableField) {
        return PurchaseOrderLineItem.builder().itemDescription(description)
                .total(BigDecimal.valueOf(unitPrice * quantity)).identifiableField(identifiableField)
                .unitPrice(BigDecimal.valueOf(unitPrice)).quantity(quantity).build();
    }

    protected float getUnitPrice() {
        /* Unit Price from float */
        float randomFloat = random.nextFloat();
        return randomFloat * random.nextInt(21);
    }
}
