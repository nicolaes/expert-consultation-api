package com.code4ro.legalconsultation.document.consolidated.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class DocumentConsultationDataDto {
    private Date consultationStartDate;
    private Date consultationDeadline;
    private Boolean excludedFromConsultation;
}
