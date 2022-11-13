package com.giftedconcepts.validation.core.model;

import com.giftedconcepts.validation.core.annotations.UnprintableCharacters;
import com.giftedconcepts.validation.core.annotations.UnprintableCharactersAllowLegal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
@UnprintableCharacters()
public class Department {

    private Long departmentId;

    private Integer floor;

    @UnprintableCharactersAllowLegal
    private String departmentLegalStatement;

    private String name;


}
