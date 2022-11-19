package com.giftedconcepts.validation.core.annotations;

import com.giftedconcepts.validation.core.config.AbstractApplicationSystemIT;
import com.giftedconcepts.validation.core.model.PurchaseOrder;
import com.giftedconcepts.validation.core.model.PurchaseOrderLineItem;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
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
    void testValidationConstraint_allow_legal_violations() {
        assertEquals(1,
                validator.getValidator()
                        .validate(buildDepartment("DepartmentName",
                                "A special character " + (char) 384)).size());
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
        assertAll("assert all violation messages",
                () -> assertThat(violations.size(), equalTo(2)),
                () -> assertThat(violations, hasItem(Matchers.<ConstraintViolationImpl<PurchaseOrder>>hasProperty("message",
                        is("Field PurchaseOrder.description has special characters which failed validation.")))),
                () -> assertThat(violations, hasItem(Matchers.<ConstraintViolationImpl<PurchaseOrder>>hasProperty("message",
                        is("Field PurchaseOrder.purchaserName has special characters which failed validation."))))
        );
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
        assertAll("assert all violation messages",
                () -> assertThat(violations.size(), equalTo(1)),
                () -> assertThat(violations, hasItem(Matchers.<ConstraintViolationImpl<PurchaseOrder>>hasProperty("message",
                        is("Field Department.name has special characters which failed validation."))))
        );
    }

    @Test
    void testValidationConstraint_null_single_child_object() {
        PurchaseOrder purchaseOrder = buildPurchaseOrder(null,
                "This is a description.",
                buildDepartment("Human Resources", null),
                buildPurchaseOrderLineItems(true, 3,
                        null, null));

        Set<ConstraintViolation<PurchaseOrder>> violations = validator.getValidator().validate(purchaseOrder);
        assertNotNull(violations);
        assertEquals(0, violations.size());
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
        assertAll("assert all violation messages",
                () -> assertThat(violations.size(), equalTo(1)),
                () -> assertThat(violations, hasItem(Matchers.<ConstraintViolationImpl<PurchaseOrder>>hasProperty("message",
                        is("Field PurchaseOrderLineItem.itemDescription has special characters which failed validation. " +
                                "The identifying field value: This is my identity."))))
        );
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
        assertAll("assert all violation messages",
                () -> assertThat(violations.size(), equalTo(1)),
                () -> assertThat(violations, hasItem(Matchers.<ConstraintViolationImpl<PurchaseOrder>>hasProperty("message",
                        is("Field PurchaseOrderLineItem.identifiableField has special characters which failed " +
                                "validation. The identifying field value: This is my identity ê."))))
        );
    }
}
