package com.code4ro.legalconsultation.document.configuration.model.dto;

import com.code4ro.legalconsultation.core.model.dto.BaseEntityDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DocumentConfigurationDto extends BaseEntityDto {
    private Boolean openForCommenting;
    private Boolean openForVotingComments;
    private Date consultationStartDate;
    private Date consultationDeadline;
    private Boolean excludedFromConsultation;
}
