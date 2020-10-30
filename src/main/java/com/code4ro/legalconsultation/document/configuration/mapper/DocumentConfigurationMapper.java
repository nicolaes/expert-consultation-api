package com.code4ro.legalconsultation.document.configuration.mapper;

import com.code4ro.legalconsultation.document.configuration.model.dto.DocumentConfigurationDto;
import com.code4ro.legalconsultation.document.configuration.model.persistence.DocumentConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentConfigurationMapper {
    DocumentConfigurationDto map(DocumentConfiguration documentConfiguration);

    DocumentConfiguration map(DocumentConfigurationDto documentConfigurationDto);
}
