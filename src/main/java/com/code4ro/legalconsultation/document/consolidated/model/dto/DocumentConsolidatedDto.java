package com.code4ro.legalconsultation.document.consolidated.model.dto;

import com.code4ro.legalconsultation.core.model.dto.BaseEntityDto;
import com.code4ro.legalconsultation.document.configuration.model.dto.DocumentConfigurationDto;
import com.code4ro.legalconsultation.document.metadata.model.dto.DocumentMetadataDto;
import com.code4ro.legalconsultation.document.node.model.dto.DocumentNodeDto;
import com.code4ro.legalconsultation.user.model.dto.UserDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class DocumentConsolidatedDto extends BaseEntityDto {
    private DocumentMetadataDto documentMetadata;
    private DocumentNodeDto documentNode;
    private DocumentConfigurationDto documentConfiguration;
    private List<UserDto> assignedUsers;
}
