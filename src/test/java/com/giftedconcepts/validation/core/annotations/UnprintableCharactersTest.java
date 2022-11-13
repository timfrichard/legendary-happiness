package com.giftedconcepts.validation.core.annotations;

import com.giftedconcepts.validation.core.config.AbstractApplicationSystemIT;
import com.giftedconcepts.validation.core.model.PurchaseOrder;
import com.giftedconcepts.validation.core.model.PurchaseOrderLineItem;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UnprintableCharactersTest extends AbstractApplicationSystemIT {

    @Autowired
    private ValidatorFactory validator;

    @BeforeEach
    public void initialize() {

        random = new Random();
    }

    @Test
    void testValidationConstraint_no_violations() {

        //"Tim Richard loads of £££££ in"
        PurchaseOrder purchaseOrder = buildPurchaseOrder("Timmy Richard",
                "This is a test description.",
                buildDepartment("Information Technology", "This is my legal statement."),
                buildPurchaseOrderLineItems(true, 3,
                        null, null));

        Set<ConstraintViolation<PurchaseOrder>> violations = validator.getValidator().validate(purchaseOrder);
        assertNotNull(violations);
        assertEquals(0, violations.size());
    }

    @Test
    void testValidationConstraint_allow_legal_no_violations() {

        /* allow bullet */
        assertEquals(0,
                validator.getValidator()
                        .validate(buildDepartment("DepartmentName",
                                "A bullet " + (char) 149)).size());
        /* allow bullet_2 */
        assertEquals(0,
                validator.getValidator()
                        .validate(buildDepartment("DepartmentName",
                                "A bullet_2 " + (char) 8226)).size());
        /* allow carriage return */
        assertEquals(0,
                validator.getValidator()
                        .validate(buildDepartment("DepartmentName",
                                "A carriage return " + (char) 13)).size());
        /* allow line feed */
        assertEquals(0,
                validator.getValidator()
                        .validate(buildDepartment("DepartmentName",
                                "A line feed " + (char) 10)).size());
        /* allow section */
        assertEquals(0,
                validator.getValidator()
                        .validate(buildDepartment("DepartmentName",
                                "A section " + (char) 167)).size());
        /* allow paragraph */
        assertEquals(0,
                validator.getValidator()
                        .validate(buildDepartment("DepartmentName",
                                "A bullet " + (char) 182)).size());
    }

    @Test
    void testValidationConstraint_violations_root_object() {
        PurchaseOrder purchaseOrder = buildPurchaseOrder("Timmy Richard has loads of £££££.",
                "This is a description. " + (char) 155 + " .",
                buildDepartment("Information Technology", "This is my legal statement."),
                buildPurchaseOrderLineItems(true, 3,
                        null, null));

        Set<ConstraintViolation<PurchaseOrder>> violations = validator.getValidator().validate(purchaseOrder);
        assertNotNull(violations);
        assertEquals(2, violations.size());
    }

    @Test
    void testValidationConstraint_violations_single_child_object() {
        PurchaseOrder purchaseOrder = buildPurchaseOrder("Timmy Richard",
                "This is a description.",
                buildDepartment("20�s", "This is my legal statement."),
                buildPurchaseOrderLineItems(true, 3,
                        null, null));

        Set<ConstraintViolation<PurchaseOrder>> violations = validator.getValidator().validate(purchaseOrder);
        assertNotNull(violations);
        assertEquals(1, violations.size());
    }

    @Test
    void testValidationConstraint_violations_set_of_child_objects() {
        List<PurchaseOrderLineItem> purchaseOrderLineItems = Lists.newArrayList();
        PurchaseOrder purchaseOrder = buildPurchaseOrder("Timmy Richard",
                "This is a description.",
                buildDepartment("Sports Department", "This is my legal statement."),
                buildPurchaseOrderLineItems(false, 0,
                        purchaseOrderLineItems, buildPurchaseOrderLineItem(random.nextInt(51),
                                getUnitPrice(), "Bad description " + (char) 234 + ".",
                                "This is my identity.")));

        Set<ConstraintViolation<PurchaseOrder>> violations = validator.getValidator().validate(purchaseOrder);
        assertNotNull(violations);
        assertEquals(1, violations.size());
    }

    @Test
    void testValidationConstraint_violations_set_of_child_objects_II() {
        List<PurchaseOrderLineItem> purchaseOrderLineItems = Lists.newArrayList();
        PurchaseOrder purchaseOrder = buildPurchaseOrder("Timmy Richard",
                "This is a description.",
                buildDepartment("Sports Department", "This is my legal statement."),
                buildPurchaseOrderLineItems(false, 0,
                        purchaseOrderLineItems, buildPurchaseOrderLineItem(random.nextInt(51),
                                getUnitPrice(), "Good description.",
                                "This is my identity " + (char) 234 + ".")));

        Set<ConstraintViolation<PurchaseOrder>> violations = validator.getValidator().validate(purchaseOrder);
        assertNotNull(violations);
        assertEquals(1, violations.size());
    }
}
